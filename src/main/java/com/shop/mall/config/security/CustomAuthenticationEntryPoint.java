package com.shop.mall.config.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		System.out.println("Unauthorized!!");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요한 기능입니다.");
//		throw new DuplicationException(ErrorCode.SC_UNAUTHORIZED.getMessage(), ErrorCode.SC_UNAUTHORIZED);
	}

}