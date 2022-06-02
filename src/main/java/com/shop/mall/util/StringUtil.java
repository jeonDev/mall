package com.shop.mall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StringUtil {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static String nullToBlank(String str) {
		
		return str == null ? "" : str;
	}
	
}
