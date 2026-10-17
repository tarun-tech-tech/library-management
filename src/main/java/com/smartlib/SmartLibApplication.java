package com.smartlib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(SmartLibProperties.class)
@EnableScheduling
public class SmartLibApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLibApplication.class, args);
    }
}
