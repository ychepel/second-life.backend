package de.ait.secondlife.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DOProperties {

    @Value("${DOProperties.accessKey}")
    private String accessKey;
    @Value("${DOProperties.secretKey}")
    private String secretKey;
    @Value("${DOProperties.endpoint}")
    private String endpoint;
    @Value("${DOProperties.region}")
    private String region;
}