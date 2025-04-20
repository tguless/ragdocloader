package com.docloader.service.impl;

import com.docloader.model.S3BucketConfig;
import com.docloader.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {

    @Qualifier("s3BucketName")
    private final String defaultBucketName;
    
    @Value("${docloader.s3.endpoint}")
    private String defaultEndpoint;
    
    @Value("${docloader.s3.region}")
    private String defaultRegion;
    
    @Value("${docloader.s3.access-key}")
    private String defaultAccessKey;
    
    @Value("${docloader.s3.secret-key}")
    private String defaultSecretKey;
    
    @Value("${docloader.s3.path-style-access}")
    private boolean defaultPathStyleAccess;
    
    // Cache S3 clients per config to avoid creating new ones for each request
    private final Map<String, S3Client> s3ClientCache = new ConcurrentHashMap<>();
    private final Map<String, S3AsyncClient> s3AsyncClientCache = new ConcurrentHashMap<>();
    
    // Default clients for backward compatibility
    private final S3Client defaultS3Client;
    private final S3AsyncClient defaultS3AsyncClient;
    
    public S3ServiceImpl(@Qualifier("s3BucketName") String defaultBucketName,
                        S3Client defaultS3Client,
                        S3AsyncClient defaultS3AsyncClient) {
        this.defaultBucketName = defaultBucketName;
        this.defaultS3Client = defaultS3Client;
        this.defaultS3AsyncClient = defaultS3AsyncClient;
    }
    
    // Get or create an S3 client for the given bucket config
    private S3Client getS3Client(S3BucketConfig config) {
        String configId = config.getId().toString();
        return s3ClientCache.computeIfAbsent(configId, id -> createS3Client(config));
    }
    
    // Get or create an S3 async client for the given bucket config
    private S3AsyncClient getS3AsyncClient(S3BucketConfig config) {
        String configId = config.getId().toString();
        return s3AsyncClientCache.computeIfAbsent(configId, id -> createS3AsyncClient(config));
    }
    
    // Create a new S3 client for the given bucket config
    private S3Client createS3Client(S3BucketConfig config) {
        String endpoint = config.getEndpoint() != null ? config.getEndpoint() : defaultEndpoint;
        String region = config.getRegion() != null ? config.getRegion() : defaultRegion;
        String accessKey = config.getAccessKey() != null ? config.getAccessKey() : defaultAccessKey;
        String secretKey = config.getSecretKey() != null ? config.getSecretKey() : defaultSecretKey;
        boolean pathStyleAccess = config.getPathStyleAccess() != null ? config.getPathStyleAccess() : defaultPathStyleAccess;
        
        log.info("Creating S3 client for bucket config {}", config.getName());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(30))
                        .build());
        
        // Set custom endpoint if configured (for MinIO)
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        // Enable path-style access if required (for MinIO)
        if (pathStyleAccess) {
            builder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return builder.build();
    }
    
    // Create a new S3 async client for the given bucket config
    private S3AsyncClient createS3AsyncClient(S3BucketConfig config) {
        String endpoint = config.getEndpoint() != null ? config.getEndpoint() : defaultEndpoint;
        String region = config.getRegion() != null ? config.getRegion() : defaultRegion;
        String accessKey = config.getAccessKey() != null ? config.getAccessKey() : defaultAccessKey;
        String secretKey = config.getSecretKey() != null ? config.getSecretKey() : defaultSecretKey;
        boolean pathStyleAccess = config.getPathStyleAccess() != null ? config.getPathStyleAccess() : defaultPathStyleAccess;
        
        log.info("Creating S3 async client for bucket config {}", config.getName());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(30))
                .build();
        
        var builder = S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(httpClient);
        
        // Set custom endpoint if configured (for MinIO)
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        // Enable path-style access if required (for MinIO)
        if (pathStyleAccess) {
            builder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return builder.build();
    }

    @Override
    public String uploadFile(S3BucketConfig config, String key, File file, Map<String, String> metadata) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }
            
            s3Client.putObject(requestBuilder.build(), RequestBody.fromFile(file));
            return getObjectUrl(config, key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public String uploadFile(S3BucketConfig config, String key, MultipartFile file, Map<String, String> metadata) {
        try {
            return uploadFile(config, key, file.getInputStream(), file.getSize(), file.getContentType(), metadata);
        } catch (IOException e) {
            log.error("Error uploading multipart file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload multipart file to S3", e);
        }
    }

    @Override
    public String uploadFile(S3BucketConfig config, String key, InputStream inputStream, long contentLength, String contentType, Map<String, String> metadata) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentLength(contentLength);
            
            if (contentType != null) {
                requestBuilder.contentType(contentType);
            }
            
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }
            
            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(inputStream, contentLength));
            return getObjectUrl(config, key);
        } catch (Exception e) {
            log.error("Error uploading input stream to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload input stream to S3", e);
        }
    }

    @Override
    public CompletableFuture<String> uploadFileAsync(S3BucketConfig config, String key, File file, Map<String, String> metadata) {
        try {
            S3AsyncClient s3AsyncClient = getS3AsyncClient(config);
            String bucketName = config.getBucketName();
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }
            
            return s3AsyncClient.putObject(requestBuilder.build(), AsyncRequestBody.fromFile(file))
                    .thenApply(response -> getObjectUrl(config, key));
        } catch (Exception e) {
            log.error("Error uploading file to S3 asynchronously: {}", e.getMessage(), e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Failed to upload file to S3 asynchronously", e));
            return future;
        }
    }

    @Override
    public InputStream downloadFile(S3BucketConfig config, String key) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            return s3Client.getObject(request);
        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public boolean doesObjectExist(S3BucketConfig config, String key) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if object exists in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check if object exists in S3", e);
        }
    }

    @Override
    public void deleteObject(S3BucketConfig config, String key) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("Error deleting object from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete object from S3", e);
        }
    }

    @Override
    public List<String> listObjects(S3BucketConfig config, String prefix) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            List<String> objects = new ArrayList<>();
            
            for (S3Object object : response.contents()) {
                objects.add(object.key());
            }
            
            return objects;
        } catch (Exception e) {
            log.error("Error listing objects in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list objects in S3", e);
        }
    }

    @Override
    public String getObjectUrl(S3BucketConfig config, String key) {
        try {
            String endpoint = config.getEndpoint() != null ? config.getEndpoint() : defaultEndpoint;
            String bucketName = config.getBucketName();
            
            if (endpoint != null && !endpoint.isEmpty()) {
                return String.format("%s/%s/%s", endpoint, bucketName, key);
            } else {
                // Use AWS S3 URL format
                String region = config.getRegion() != null ? config.getRegion() : defaultRegion;
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            }
        } catch (Exception e) {
            log.error("Error generating object URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate object URL", e);
        }
    }

    @Override
    public void createBucketIfNotExists(S3BucketConfig config) {
        try {
            S3Client s3Client = getS3Client(config);
            String bucketName = config.getBucketName();
            
            createBucketIfNotExistsInternal(s3Client, bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create bucket in S3", e);
        }
    }

    @Override
    public void createBucketIfNotExists(String bucketName) {
        try {
            createBucketIfNotExistsInternal(defaultS3Client, bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create bucket in S3", e);
        }
    }

    private void createBucketIfNotExistsInternal(S3Client s3Client, String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            try {
                s3Client.headBucket(headBucketRequest);
                log.info("S3 bucket '{}' already exists", bucketName);
            } catch (NoSuchBucketException e) {
                log.info("Creating S3 bucket '{}'", bucketName);
                
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                
                s3Client.createBucket(createBucketRequest);
                
                log.info("S3 bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check/create bucket in S3", e);
        }
    }
} 