package com.example.myFarm;

// import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// [최종 수정] 모든 매퍼 패키지를 포함하는 최상위 경로를 스캔
@SpringBootApplication
@EnableScheduling
// @MapperScan(basePackages = "com.example.myFarm") // <--- 최상위 패키지 경로로 변경
public class MyFarmApplication {

	public static void main(String[] args) {

        SpringApplication.run(MyFarmApplication.class, args);
	}

}
