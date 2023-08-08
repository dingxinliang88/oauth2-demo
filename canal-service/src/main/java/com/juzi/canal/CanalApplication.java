package com.juzi.canal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author codejuzi
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CanalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanalApplication.class, args);
    }
}