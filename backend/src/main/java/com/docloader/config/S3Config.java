package com.docloader.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    @Value("${docloader.s3.access-key}")
    private String accessKey;

    @Value("${docloader.s3.secret-key}")
    private String secretKey;
    
    @Value("${docloader.s3.region}")
    private String region;
    
    @Value("${docloader.s3.endpoint}")
    private String endpoint;
    
    @Value("${docloader.s3.path-style-access}")
    private boolean pathStyleAccess;
    
    @Value("${docloader.s3.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(30))
                        .build());
        
        // For local testing with MinIO, we use a custom endpoint and path-style access
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        if (pathStyleAccess) {
            builder.serviceConfiguration(
                    software.amazon.awssdk.services.s3.S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return builder.build();
    }
    
    @Bean
    public S3AsyncClient s3AsyncClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(30))
                .build();
        
        var builder = S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(region))
                .httpClient(httpClient);
        
        // For local testing with MinIO, we use a custom endpoint and path-style access
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        if (pathStyleAccess) {
            builder.serviceConfiguration(
                    software.amazon.awssdk.services.s3.S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }
        
        return builder.build();
    }
    
    @Bean(name = "s3BucketName")
    public String s3BucketName() {
        return bucketName;
    }
} 