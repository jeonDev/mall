package com.shop.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import com.shop.mall.config.security.CustomAccessDeniedHandler;
import com.shop.mall.config.security.CustomAuthenticationEntryPoint;
import com.shop.mall.config.security.JwtAuthenticationFilter;
import com.shop.mall.config.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	private final JwtTokenProvider jwtTokenProvider;
	
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	
	private final CustomAccessDeniedHandler accessDeniedHandler;
	
	// 암호화에 필요한 PasswordEncoder를 Bean 등록
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	// authenticationManager를 Bean 등록
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	/*
	 * 스프링 시큐리티 규칙 설정
	 * authorizeRequests() : 보호된 리소스 URI에 접근할 수 있는 권한을 설정
	 * antMatchers()
	 * permitAll() : 전체 접근 허용
	 * hasRole() : 특정 롤(권한)을 가진 사용자만 접근 허용
	 * */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.httpBasic().disable()		// rest api 만을 고려하여 기본설정은 해제.
		.csrf().disable()			// csrf 보안 토큰 disable 처리.
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 X
		.and()
		.authorizeRequests()		// 요청에 대한 사용권한 체크
		.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()		// CORS PreFlight 403 에러 방지.
		.antMatchers("/admin/**").hasRole("ADMIN")						// ADMIN 권한
		.antMatchers("/manager/**").hasAnyRole("MANAGE", "ADMIN")		// 관리자 권한 
		.antMatchers("/user/**").hasAnyRole("USER", "ADMIN")			// 사용자 권한
		.anyRequest().permitAll()
		.and()
		.exceptionHandling()
		.authenticationEntryPoint(authenticationEntryPoint)
		.accessDeniedHandler(accessDeniedHandler)
		.and()
		.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 넣는다.
		
		http.logout().disable();
	}
	
	/*
	 * 스프링 시큐리티 룰을 무시하게 하는 URL 설정
	 * */
	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
	}	
}
