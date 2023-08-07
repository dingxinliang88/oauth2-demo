package com.juzi.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "tencent.sms.template-id")
public class TencentSmsTemplateConfig {
    private String phoneCode;
    private String sales;
}
