package com.pe.limon.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class LimOnApiApplication {
    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(
                System.getProperty("spring.application.timezone", "UTC")
        ));
    }
    public static void main(String[] args) {
        SpringApplication.run(LimOnApiApplication.class, args);
    }

}
