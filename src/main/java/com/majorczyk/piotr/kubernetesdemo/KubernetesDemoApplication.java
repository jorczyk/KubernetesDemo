package com.majorczyk.piotr.kubernetesdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@RestController
@EnableScheduling
public class KubernetesDemoApplication {

	Logger logger = LoggerFactory.getLogger(KubernetesDemoApplication.class);

	@Value("${sense.of.life:0}")
	private Integer senseOfLife;

	private boolean isHealthy = true;
	private boolean isReady = false;
	private int unhealthyCounter = 0;
	private int notReadyCounter = 0;

	@Scheduled(initialDelay = 10000, fixedDelay = Integer.MAX_VALUE)
	public void setReadinessAfterTime() {
		isReady = true;
		logger.info("Service is ready");
	}

	@GetMapping("/")
	public String hello() {
		return "Hello SpringBoot world \n";
	}

	@GetMapping("/host")
	public String host() {
		String podName = System.getenv("HOSTNAME");
		return podName==null ? "No host name setup" : String.format("Hostname is: %s", podName);
	}

	@GetMapping("/props")
	public String props() throws IOException {
		logger.info("Getting value of life: " + senseOfLife.toString());
		logger.info("Getting meaning of life: " + getPropertiesFile("films.properties"));
		return "Sense of life: " + senseOfLife.toString() + "\nLoaded properties file: " + getPropertiesFile("films.properties");
	}

	@GetMapping(value = "/health", produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> health() {
		if(!isHealthy) {
			logger.warn("Service unhealthy! Try: " + ++unhealthyCounter);
			return ResponseEntity.internalServerError().build();
		}
		unhealthyCounter=0;
		return ResponseEntity.ok("Healthy!");
	}

	@GetMapping(value = "/readiness", produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> readiness() {
		if(!isReady) {
			logger.warn("Service not ready! Try: " + ++notReadyCounter);
			return ResponseEntity.internalServerError().build();
		}
		notReadyCounter = 0;
		return ResponseEntity.ok("Ready!");
	}

	@PostMapping("/setUnhealthy")
	public String setUnhealthy() {
		isHealthy = false;
		logger.info("Health check set to failing");
		return "Service was set to unhealthy";
	}

	private Properties getPropertiesFile(String filename) throws IOException {
		String rootPath = "/config/";
		String appConfigPath = rootPath + filename;

		Properties properties = new Properties();
		properties.load(new FileInputStream(appConfigPath));
		return properties;
	}

	public static void main(String[] args) {
		SpringApplication.run(KubernetesDemoApplication.class, args);
	}
}
