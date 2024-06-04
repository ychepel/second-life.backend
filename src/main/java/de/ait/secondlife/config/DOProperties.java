package de.ait.secondlife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "do")
public class DOProperties {

    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region;


}