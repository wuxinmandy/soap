package com.example.soap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SoapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoapApplication.class, args);
    }
}
