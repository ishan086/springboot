package com.example.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class S3ObjectProcessor implements ItemProcessor<List<String>, List<String>> {

    private final S3Client s3Client;
    private final String bucketName;

    public S3ObjectProcessor(S3Client s3Client, @Value("${s3.bucket.name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public List<String> process(List<String> objectKeys) {
        return objectKeys.parallelStream().map(key -> {
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
                String content = new String(objectBytes.asByteArray(), StandardCharsets.UTF_8);

                return "Processed: " + key + " - Content: " + content;
            } catch (Exception e) {
                return "Error processing: " + key + " - " + e.getMessage();
            }
        }).collect(Collectors.toList());
    }
}
