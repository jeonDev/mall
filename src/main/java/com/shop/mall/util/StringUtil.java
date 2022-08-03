package com.shop.mall.util;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class StringUtil {

	public static String nullToBlank(String str) {
		
		return str == null ? "" : str;
	}
	
}
