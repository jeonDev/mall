package com.shop.mall.repository;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.shop.mall.model.CmmnUser;

@Repository
public interface UserDao {
	public int insertUserJoin(HashMap<String, Object> user); // 회원가입
	
	public CmmnUser selectUserName(String id);
	
	public CmmnUser selectUserLogin(HashMap<String, Object> user);
	
	public int setRefreshToken(HashMap<String, Object> param);
	public CmmnUser getRefreshTokenToUserInfo(String refreshToken);
	
	public HashMap<String, Object> selectUserInfo(HashMap<String, Object> param);	// 사용자 정보 조회
	public int updateCmmnUser(HashMap<String, Object> param);						// 사용자 정보 수정
}
