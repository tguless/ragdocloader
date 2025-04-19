package com.docloader.service.impl;

import com.docloader.model.Tenant;
import com.docloader.service.S3Service;
import com.docloader.service.TenantService;
import com.docloader.multitenancy.TenantContext;
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

    private final TenantService tenantService;
    
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
    
    // Cache S3 clients per tenant to avoid creating new ones for each request
    private final Map<String, S3Client> s3ClientCache = new ConcurrentHashMap<>();
    private final Map<String, S3AsyncClient> s3AsyncClientCache = new ConcurrentHashMap<>();
    
    // Default clients for backward compatibility
    private final S3Client defaultS3Client;
    private final S3AsyncClient defaultS3AsyncClient;
    
    public S3ServiceImpl(TenantService tenantService, 
                        @Qualifier("s3BucketName") String defaultBucketName,
                        S3Client defaultS3Client,
                        S3AsyncClient defaultS3AsyncClient) {
        this.tenantService = tenantService;
        this.defaultBucketName = defaultBucketName;
        this.defaultS3Client = defaultS3Client;
        this.defaultS3AsyncClient = defaultS3AsyncClient;
    }
    
    // Get or create an S3 client for the given tenant
    private S3Client getS3Client(Tenant tenant) {
        String tenantId = tenant.getId().toString();
        return s3ClientCache.computeIfAbsent(tenantId, id -> createS3Client(tenant));
    }
    
    // Get or create an S3 async client for the given tenant
    private S3AsyncClient getS3AsyncClient(Tenant tenant) {
        String tenantId = tenant.getId().toString();
        return s3AsyncClientCache.computeIfAbsent(tenantId, id -> createS3AsyncClient(tenant));
    }
    
    // Create a new S3 client for the given tenant
    private S3Client createS3Client(Tenant tenant) {
        String endpoint = tenant.getS3Endpoint() != null ? tenant.getS3Endpoint() : defaultEndpoint;
        String region = tenant.getS3Region() != null ? tenant.getS3Region() : defaultRegion;
        String accessKey = tenant.getS3AccessKey() != null ? tenant.getS3AccessKey() : defaultAccessKey;
        String secretKey = tenant.getS3SecretKey() != null ? tenant.getS3SecretKey() : defaultSecretKey;
        boolean pathStyleAccess = tenant.getS3PathStyleAccess() != null ? tenant.getS3PathStyleAccess() : defaultPathStyleAccess;
        
        log.info("Creating S3 client for tenant {}", tenant.getName());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        S3Client.Builder s3ClientBuilder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(30))
                        .build());
        
        // Set custom endpoint if configured (for MinIO)
        if (endpoint != null && !endpoint.isEmpty()) {
            s3ClientBuilder.endpointOverride(URI.create(endpoint));
        }
        
        // Enable path-style access if required (for MinIO)
        if (pathStyleAccess) {
            s3ClientBuilder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return s3ClientBuilder.build();
    }
    
    // Create a new S3 async client for the given tenant
    private S3AsyncClient createS3AsyncClient(Tenant tenant) {
        String endpoint = tenant.getS3Endpoint() != null ? tenant.getS3Endpoint() : defaultEndpoint;
        String region = tenant.getS3Region() != null ? tenant.getS3Region() : defaultRegion;
        String accessKey = tenant.getS3AccessKey() != null ? tenant.getS3AccessKey() : defaultAccessKey;
        String secretKey = tenant.getS3SecretKey() != null ? tenant.getS3SecretKey() : defaultSecretKey;
        boolean pathStyleAccess = tenant.getS3PathStyleAccess() != null ? tenant.getS3PathStyleAccess() : defaultPathStyleAccess;
        
        log.info("Creating S3 async client for tenant {}", tenant.getName());
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(30))
                .build();
        
        S3AsyncClient.Builder s3AsyncClientBuilder = S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(httpClient);
        
        // Set custom endpoint if configured (for MinIO)
        if (endpoint != null && !endpoint.isEmpty()) {
            s3AsyncClientBuilder.endpointOverride(URI.create(endpoint));
        }
        
        // Enable path-style access if required (for MinIO)
        if (pathStyleAccess) {
            s3AsyncClientBuilder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return s3AsyncClientBuilder.build();
    }
    
    private String getBucketName(Tenant tenant) {
        return tenant.getS3BucketName() != null ? tenant.getS3BucketName() : defaultBucketName;
    }

    @Override
    public String uploadFile(Tenant tenant, String key, File file, Map<String, String> metadata) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }
            
            s3Client.putObject(requestBuilder.build(), RequestBody.fromFile(file));
            return getObjectUrl(tenant, key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public String uploadFile(Tenant tenant, String key, MultipartFile file, Map<String, String> metadata) {
        try {
            return uploadFile(tenant, key, file.getInputStream(), file.getSize(), file.getContentType(), metadata);
        } catch (IOException e) {
            log.error("Error uploading multipart file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload multipart file to S3", e);
        }
    }

    @Override
    public String uploadFile(Tenant tenant, String key, InputStream inputStream, long contentLength, String contentType, Map<String, String> metadata) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
            Map<String, String> metadataMap = new HashMap<>();
            if (metadata != null) {
                metadataMap.putAll(metadata);
            }
            if (contentType != null) {
                metadataMap.put("Content-Type", contentType);
            }

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            
            if (!metadataMap.isEmpty()) {
                requestBuilder.metadata(metadataMap);
            }
            
            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(inputStream, contentLength));
            return getObjectUrl(tenant, key);
        } catch (Exception e) {
            log.error("Error uploading input stream to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload input stream to S3", e);
        }
    }

    @Override
    public CompletableFuture<String> uploadFileAsync(Tenant tenant, String key, File file, Map<String, String> metadata) {
        try {
            S3AsyncClient s3AsyncClient = getS3AsyncClient(tenant);
            String bucketName = getBucketName(tenant);
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }
            
            return s3AsyncClient.putObject(requestBuilder.build(), AsyncRequestBody.fromFile(file))
                    .thenApply(response -> getObjectUrl(tenant, key));
        } catch (Exception e) {
            log.error("Error uploading file asynchronously to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file asynchronously to S3", e);
        }
    }

    @Override
    public InputStream downloadFile(Tenant tenant, String key) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            return s3Client.getObject(request);
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", key);
            throw new RuntimeException("File not found in S3: " + key, e);
        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public boolean doesObjectExist(Tenant tenant, String key) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
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
    public void deleteObject(Tenant tenant, String key) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
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
    public List<String> listObjects(Tenant tenant, String prefix) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            List<String> keys = new ArrayList<>();
            
            response.contents().forEach(s3Object -> keys.add(s3Object.key()));
            return keys;
        } catch (Exception e) {
            log.error("Error listing objects in S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list objects in S3", e);
        }
    }

    @Override
    public String getObjectUrl(Tenant tenant, String key) {
        try {
            String bucketName = getBucketName(tenant);
            String endpoint = tenant.getS3Endpoint() != null ? tenant.getS3Endpoint() : defaultEndpoint;
            
            if (endpoint != null && !endpoint.isEmpty()) {
                // For MinIO or custom endpoint
                return new URI(endpoint + "/" + bucketName + "/" + key).toString();
            } else {
                // For AWS S3
                S3Client s3Client = getS3Client(tenant);
                return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toString();
            }
        } catch (URISyntaxException e) {
            log.error("Error creating object URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create object URL", e);
        }
    }

    @Override
    public void createBucketIfNotExists(Tenant tenant) {
        try {
            S3Client s3Client = getS3Client(tenant);
            String bucketName = getBucketName(tenant);
            
            createBucketIfNotExistsInternal(s3Client, bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket in S3 for tenant {}: {}", tenant.getName(), e.getMessage(), e);
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
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
        
        boolean bucketExists = listBucketsResponse.buckets().stream()
                .anyMatch(bucket -> bucket.name().equals(bucketName));
        
        if (!bucketExists) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.createBucket(createBucketRequest);
            log.info("Created S3 bucket: {}", bucketName);
        }
    }
} 