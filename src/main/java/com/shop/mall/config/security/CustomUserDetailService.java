package com.shop.mall.config.security;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shop.mall.model.CmmnUser;
import com.shop.mall.repository.UserDao;

@Service
public class CustomUserDetailService implements UserDetailsService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserDao dao;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("loadUserByUsername 호출!! (id : " + username + ")");
		
		CmmnUser member = dao.selectUserName(username);

		if(member == null) throw new UsernameNotFoundException("가입되지 않은 계정입니다.");
		return member;
	}
	
	/*
	 * Refresh Token DB 저장
	 * param : id
	 * 			refreshToken
	 * */
	public int setRefreshToken(HashMap<String, Object> loginParam) {
		return dao.setRefreshToken(loginParam);
	}
	
	public CmmnUser getRefreshTokenToUserInfo(String refreshToken) {
		CmmnUser user = dao.getRefreshTokenToUserInfo(refreshToken);
		return user;
	}
}
