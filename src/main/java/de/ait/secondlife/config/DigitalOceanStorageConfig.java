package de.ait.secondlife.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DigitalOceanStorageConfig {

    @Value("${DOProperties.accessKey}")
    private String accessKey;
    @Value("${DOProperties.secretKey}")
    private String secretKey;
    @Value("${DOProperties.endpoint}")
    private String endpoint;
    @Value("${DOProperties.region}")
    private String region;

    @Bean
    public AmazonS3 amazonClient() {

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(endpointConfiguration);

        return builder.build();
    }
}