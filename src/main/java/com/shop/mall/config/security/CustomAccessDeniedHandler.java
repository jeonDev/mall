package com.shop.mall.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		log.info("Access Denied!!");
		// response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없는 사용자입니다.\n관리자에게 문의바랍니다.");
		throw new DuplicationException(ErrorCode.SC_FORBIDDEN.getMessage(), ErrorCode.SC_FORBIDDEN);
	}

}
