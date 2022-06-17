package com.shop.mall.config.security;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.shop.mall.model.CmmnUser;
import com.shop.mall.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
	
	private String secretKey = "mallAccessProject";
	private String refreshKey = "mallRefreshProject";
	
	// 토큰 유효시간
	private long tokenValidTime = 30 * 60 * 1000L;
	private long refreshTokenValidTime = 7 * 24 * 60 * 60 * 1000L;
	
	// service
	private final CustomUserDetailService userDetailsService;
	
	// 객체 초기화, secretKey를 Base64로 인코딩 (Byte -> String)
	@PostConstruct
	protected void init() {
		secretKey  = Base64.getEncoder().encodeToString(secretKey.getBytes());
		refreshKey = Base64.getEncoder().encodeToString(refreshKey.getBytes());
	}
	
	// JWT 토큰 생성
	public String createToken(String userPk, List<String> roles) {
		System.out.println("Token 생성중...");
		
		Claims claims = Jwts.claims().setSubject(userPk); // JWT payload에 저장되는 정보단위
		claims.put("roles", roles);	// 정보는 key value 형태로 저장
		Date now = new Date();
		return Jwts.builder()
				.setClaims(claims)	// 정보 저장	
				.setIssuedAt(now)	// 토큰 발행 시간 정보
				.setExpiration(new Date(now.getTime() + tokenValidTime))	// set Expire Time
				.signWith(SignatureAlgorithm.HS256, secretKey)				// 사용할 암호화 알고리즘과 signature에 들어갈 secret값 세팅
				.compact();
	}
	
	public String createRefreshToken(String userPk) {
		Date now = new Date();
		
		Claims claims = Jwts.claims().setSubject(userPk); // JWT payload에 저장되는 정보단위
		
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + refreshTokenValidTime))
				.signWith(SignatureAlgorithm.HS256, refreshKey)
				.compact();
	}
	
	// JWT 토큰에서 인증 정보 조회
	public Authentication getAuthentication(String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}
	
	// 토큰에서 회원 정보 추출
	public String getUserPk(String token) {
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
	}
	
	// Request의 Header에서 token 값을 가져온다. "Authorization" : "TOKEN값"
	public String resolveToken(HttpServletRequest request) {
		String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
		return accessToken;
	}
	
	// Request의 Header Cookie에서 Refresh Token 값을 가져온다.
	public String getRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		
		if(cookies != null && cookies.length > 0) {
			for(Cookie c : cookies) {
				if(c.getName().equals("refreshToken")) {
					return c.getValue();
				}
			}
		}
		
		return null;
	}
	
	// 토큰의 유효성 + 만료일자 확인
	public boolean validateToken(String jwtToken) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
			return !claims.getBody().getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
	
	// 토큰의 유효성 + 만료일자 확인(Refresh Token)
	public boolean validateRefreshToken(String jwtToken) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(refreshKey).parseClaimsJws(jwtToken);
			return !claims.getBody().getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
	
	// 유저 정보 Get
	public CmmnUser getUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		CmmnUser user = new CmmnUser();
		
		if(authentication.getPrincipal() instanceof CmmnUser) {
			user = (CmmnUser) authentication.getPrincipal();
		}
		
		return user;
	}
	
	/*
	 * Refresh Token 재 발급
	 * */
	public void setRefreshToken(String token, HttpServletResponse response) {
		
		HashMap<String, Object> loginParam = new HashMap<String, Object>();
		
		CmmnUser user = this.getUserInfo();
		
		String refreshToken = this.createRefreshToken(user.getUsername());
		
		
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(60*60*24*7);
		
		String id = StringUtil.nullToBlank(user.getId());
		loginParam.put("id", id);
		loginParam.put("refreshToken", refreshToken);
		
		Cookie initCookie = new Cookie("refreshToken", null);
		initCookie.setMaxAge(0); // 유효시간을 0으로 설정
    	response.addCookie(initCookie); // 응답 헤더에 추가해서 없어지도록 함
		
//		response.addCookie(cookie);
		
		System.out.println("새로운 Refresh Token : " + refreshToken);
//		userDetailsService.setRefreshToken(loginParam);
	}
	
	// Refresh Token으로 User 정보 추출
	public CmmnUser getRefreshTokenToUserInfo(String refreshToken) {
		return userDetailsService.getRefreshTokenToUserInfo(refreshToken);
	}
}
