package com.hoxy133.llopharm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LloPharmApplication {

    public static void main(String[] args) {
        SpringApplication.run(LloPharmApplication.class, args);
    }

}
