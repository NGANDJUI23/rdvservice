package com.pkfrc.rdvservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
//@EntityScan(basePackages = "com.pkfrc.rdvservice.entity")
public class RdvserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RdvserviceApplication.class, args);
    }

}
