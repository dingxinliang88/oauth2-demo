package com.juzi.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author codejuzi
 */
@Data
@Component
@ConfigurationProperties(prefix = "third-party.gitee")
public class GiteeConfig {
    private String clientId;
    private String clientSecret;
    private String callBack;
    private String tokenUrl;
    private String userUrl;
    private String state;
}
