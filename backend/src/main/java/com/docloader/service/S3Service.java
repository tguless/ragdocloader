package com.docloader.service;

import com.docloader.model.S3BucketConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for interacting with S3 or S3-compatible storage (MinIO for local development)
 */
public interface S3Service {

    /**
     * Upload a file to the storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key (path and filename)
     * @param file The file to upload
     * @param metadata Optional metadata to attach to the object
     * @return The full URL of the uploaded object
     */
    String uploadFile(S3BucketConfig config, String key, File file, Map<String, String> metadata);

    /**
     * Upload a multipart file to the storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key (path and filename)
     * @param file The multipart file to upload
     * @param metadata Optional metadata to attach to the object
     * @return The full URL of the uploaded object
     */
    String uploadFile(S3BucketConfig config, String key, MultipartFile file, Map<String, String> metadata);

    /**
     * Upload data from an input stream to the storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key (path and filename)
     * @param inputStream The input stream to read data from
     * @param contentLength The length of the content in bytes
     * @param contentType The content type (MIME type)
     * @param metadata Optional metadata to attach to the object
     * @return The full URL of the uploaded object
     */
    String uploadFile(S3BucketConfig config, String key, InputStream inputStream, long contentLength, String contentType, Map<String, String> metadata);

    /**
     * Upload a file asynchronously using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key (path and filename)
     * @param file The file to upload
     * @param metadata Optional metadata to attach to the object
     * @return A CompletableFuture that will resolve to the full URL of the uploaded object
     */
    CompletableFuture<String> uploadFileAsync(S3BucketConfig config, String key, File file, Map<String, String> metadata);

    /**
     * Download a file from storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key (path and filename)
     * @return An input stream containing the file data
     */
    InputStream downloadFile(S3BucketConfig config, String key);

    /**
     * Check if an object exists in the storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key to check
     * @return True if the object exists, false otherwise
     */
    boolean doesObjectExist(S3BucketConfig config, String key);

    /**
     * Delete an object from storage using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key to delete
     */
    void deleteObject(S3BucketConfig config, String key);

    /**
     * List objects in a directory/prefix using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param prefix The prefix/directory to list
     * @return A list of object keys
     */
    List<String> listObjects(S3BucketConfig config, String prefix);

    /**
     * Get the full URL for an object using a specific bucket configuration
     * @param config The S3 bucket configuration
     * @param key The object key
     * @return The full URL
     */
    String getObjectUrl(S3BucketConfig config, String key);
    
    /**
     * Create a bucket using a specific bucket configuration if it doesn't exist
     * @param config The S3 bucket configuration
     */
    void createBucketIfNotExists(S3BucketConfig config);
    
    /**
     * For backward compatibility - initializes default bucket
     * @param bucketName The name of the bucket to create
     */
    void createBucketIfNotExists(String bucketName);
    
    /**
     * Upload a file using the default S3 configuration 
     * @param key The object key (path and filename)
     * @param inputStream The input stream to read data from
     * @param contentLength The length of the content in bytes
     * @param contentType The content type (MIME type)
     * @return The full URL of the uploaded object
     */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);
    
    /**
     * Delete a file using the default S3 configuration
     * @param key The object key to delete
     */
    void deleteFile(String key);
    
    /**
     * Check if a file exists using the default S3 configuration
     * @param key The object key to check
     * @return True if the object exists, false otherwise
     */
    boolean fileExists(String key);
} 