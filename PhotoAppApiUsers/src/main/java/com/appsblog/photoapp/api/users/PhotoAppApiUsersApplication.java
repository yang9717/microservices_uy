package com.appsblog.photoapp.api.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PhotoAppApiUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoAppApiUsersApplication.class, args);
	}
	
	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
