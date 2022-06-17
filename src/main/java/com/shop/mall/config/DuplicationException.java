package com.shop.mall.config;

import com.shop.mall.util.StringUtil;

import lombok.Getter;

@Getter
public class DuplicationException extends RuntimeException{

	private ErrorCode errorCode;
	
	public DuplicationException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
