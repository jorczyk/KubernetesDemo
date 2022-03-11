package com.majorczyk.piotr.kubernetesdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KubernetesDemoApplication {

	@GetMapping("/")
	public String hello() {
		return "Hello SpringBoot world \n";
	}

	@GetMapping("/host")
	public String host() {
		String podName = System.getenv("HOSTNAME");
		return podName==null ? "No host name setup" : String.format("Hostname is: %s", podName);
	}

	public static void main(String[] args) {
		SpringApplication.run(KubernetesDemoApplication.class, args);
	}
}
