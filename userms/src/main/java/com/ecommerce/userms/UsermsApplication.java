package com.ecommerce.userms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class UsermsApplication {

	private static final Logger log = LoggerFactory.getLogger(UsermsApplication.class);

	@Value("${spring.data.mongodb.uri:NOT_SET}")
	private String mongoUri;

	@Value("${spring.data.mongodb.host:NOT_SET}")
	private String mongoHost;

	@Value("${spring.data.mongodb.database:NOT_SET}")
	private String mongoDatabase;

	private final Environment env;

	public UsermsApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		SpringApplication.run(UsermsApplication.class, args);
	}

	@PostConstruct
	public void printConfig() {
		log.info("========================================");
		log.info("CONFIGURATION RECEIVED:");
		log.info("========================================");
		log.info("Active Profiles: {}", String.join(", ", env.getActiveProfiles()));
		log.info("MongoDB URI: {}", mongoUri);
		log.info("MongoDB Host: {}", mongoHost);
		log.info("MongoDB Database: {}", mongoDatabase);
		log.info("Config Server URI: {}", env.getProperty("spring.cloud.config.uri"));
		log.info("========================================");
	}
}