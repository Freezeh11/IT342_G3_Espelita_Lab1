package com.it342.espelita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.it342.espelita") // This forces Spring to scan everything in your project
public class EspelitaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EspelitaApplication.class, args);
    }
}