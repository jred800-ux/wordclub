package com.jred.wordclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WordclubApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordclubApplication.class, args);
    }

}
