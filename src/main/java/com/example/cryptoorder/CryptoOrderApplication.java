package com.example.cryptoorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CryptoOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoOrderApplication.class, args);
    }

}
