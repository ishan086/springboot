package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.reader.S3ObjectReader;
import com.example.batch.processor.S3ObjectProcessor;
import com.example.batch.writer.S3ObjectWriter;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, 
                      S3ObjectReader s3ObjectReader, S3ObjectProcessor s3ObjectProcessor, S3ObjectWriter s3ObjectWriter) {
        return new StepBuilder("step1", jobRepository)
                .<List<String>, List<String>>chunk(1, transactionManager)
                .reader(s3ObjectReader)
                .processor(s3ObjectProcessor)
                .writer(s3ObjectWriter)
                .build();
    }
    
    @Bean
    public Job job(JobRepository jobRepository, Step step1) {
        return new JobBuilder("s3Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }
}
