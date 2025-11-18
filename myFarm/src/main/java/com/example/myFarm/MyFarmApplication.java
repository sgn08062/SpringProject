package com.example.myFarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyFarmApplication {

	public static void main(String[] args) {

        SpringApplication.run(MyFarmApplication.class, args);
	}

}
