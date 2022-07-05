package com.shop.mall.config;

import com.shop.mall.util.StringUtil;

import lombok.Getter;

@Getter
public class DuplicationException extends RuntimeException{

	private ErrorCode errorCode;
	
	public DuplicationException(String message, ErrorCode errorCode) {
		super(message);
		message = StringUtil.nullToBlank(message);
		if(!"".equals(message)) errorCode.setMessage(message);
		this.errorCode = errorCode;
	}
}
