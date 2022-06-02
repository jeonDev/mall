package com.shop.mall.repository;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CommonDao {
	public int insertCmmnFile(HashMap<String, Object> param);	// 공통 파일 INSERT
	
	public List<HashMap<String, Object>> selectCmmnCodeList(HashMap<String, Object> param);	// 공통 코드 조회
	
	public List<HashMap<String, Object>> selectCmmnMenuList(HashMap<String, Object> param);	// 공통 메뉴 조회
	
	public int createCertNum(HashMap<String, Object> param);	// 인증번호 요청
	public HashMap<String, Object> certNumCheck(HashMap<String, Object> param);	// 인증번호 확인
}
