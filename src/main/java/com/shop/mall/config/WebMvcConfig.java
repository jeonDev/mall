package com.shop.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

	/*
	 * Cors (localhost:8001)
	 * http://43.200.39.6:8001/
	 * */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:8001")
//				.allowedOrigins("http://43.200.39.6:8001/")
				.maxAge(3600)				// 3600초 동안 preflight 결과를 캐시에 저장
				.allowCredentials(true)		// Cookie 요청 허용
				;
	}
	
}
