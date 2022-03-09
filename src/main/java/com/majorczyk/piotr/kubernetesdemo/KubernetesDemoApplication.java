package com.majorczyk.piotr.kubernetesdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KubernetesDemoApplication {

	@GetMapping("/")
	public String hello() {
		return "Hello SpringBoot world \n";
	}

	public static void main(String[] args) {
		SpringApplication.run(KubernetesDemoApplication.class, args);
	}

}
