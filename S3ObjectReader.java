package com.example.batch.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class S3ObjectReader implements ItemReader<List<String>> {

    private final S3Client s3Client;
    private final String bucketName;
    private final IteratorItemReader<List<String>> delegate;

    public S3ObjectReader(S3Client s3Client, @Value("${s3.bucket.name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;

        List<String> objectKeys = s3Client.listObjectsV2(req -> req.bucket(bucketName)).contents()
                .stream()
                .filter(obj -> obj.lastModified().isAfter(Instant.now().minus(24, ChronoUnit.HOURS)))
                .map(S3Object::key)
                .collect(Collectors.toList());

        List<List<String>> chunks = IntStream.range(0, (objectKeys.size() + 9) / 10)
                .mapToObj(i -> objectKeys.subList(i * 10, Math.min(objectKeys.size(), (i + 1) * 10)))
                .collect(Collectors.toList());

        this.delegate = new IteratorItemReader<>(chunks.iterator());
    }

    @Override
    public List<String> read() {
        return delegate.read();
    }
}
