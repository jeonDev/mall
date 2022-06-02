package com.shop.mall.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.message.AuthException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shop.mall.model.CmmnUser;
import com.shop.mall.service.UserService;
import com.shop.mall.util.StringUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final JwtTokenProvider jwtTokenProvider;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		logger.info("doFilter 호출!!");
		
		// 헤더에서 JWT를 받아옴.
		String token = jwtTokenProvider.resolveToken(request);
		
		logger.info("Access Token : " + token);
		logger.info(request.getRequestURI());
		// 유효한 토큰인지 확인.
		if(token != null) {
			// Access Token 유효성 체크
			if(jwtTokenProvider.validateToken(token)) {
				this.setConetxtHolder(token);
			}
			
			String refreshToken = jwtTokenProvider.getRefreshToken(request);
			
			if(!"".equals(refreshToken) &&
					!jwtTokenProvider.validateRefreshToken(refreshToken)) {
				System.out.println("Access Token 유효 !\nRefresh Token 유효기간 지남.");
				//jwtTokenProvider.setRefreshToken(token, response);
			}
			
		}
		
		filterChain.doFilter(request, response);
	}

	public void setConetxtHolder(String accessToken) {
		// 토큰이 유효하면 토큰으로부터 유저 정보를 받아옴.
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		
		// SecurityContext에 Authentication 객체를 저장.
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
