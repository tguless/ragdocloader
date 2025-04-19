package com.docloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocLoaderApplication.class, args);
    }
} 