package com.ashish.shopoclock;


import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ShopOClock {

	@Value("${spring.cloudinary.cloudinary_name}")
	private String cloudName;

	@Value("${spring.cloudinary.cloudinary_api_key}")
	private String apiKey;

	@Value("${spring.cloudinary.cloudinary_api_secret}")
	private String apiSecret;



	public static void main(String[] args) {
		SpringApplication.run(ShopOClock.class, args);
	}


	@Bean
	public Cloudinary getCloudinary() {
		Cloudinary cloudinary = null;
		Map config = new HashMap();
		config.put("cloud_name", cloudName);
		config.put("api_key", apiKey);
		config.put("api_secret", apiSecret);
		config.put("secure", true);
		cloudinary = new Cloudinary(config);
		return cloudinary;
	}

}