package com.greentech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class GreentechApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreentechApplication.class, args);
    }
}
