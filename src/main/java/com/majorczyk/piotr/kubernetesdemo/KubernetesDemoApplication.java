package com.majorczyk.piotr.kubernetesdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@RestController
public class KubernetesDemoApplication {

	@Value("${sense.of.life:0}")
	private Integer senseOfLife;

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
		System.out.println("Getting value of life: " + senseOfLife.toString());
		System.out.println("Getting meaning of life: " + getPropertiesFile("hehe.properties"));
		return "Sense of life: " + senseOfLife.toString() + "\nLoaded properties file: " + getPropertiesFile("hehe.properties");
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
