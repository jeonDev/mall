package com.shop.mall.repository;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface BoardDao {

	public List<HashMap<String, Object>> selectBbsMstList(HashMap<String, Object> param);	// 게시판 리스트 조회
	public HashMap<String, Object> selectBbsMstOne(HashMap<String, Object> param);	// 게시판 한 건 조회
	public int selectBbsMstListCount(HashMap<String, Object> param);	// 게시판 리스트 건수 조회
	public int insertBoard(HashMap<String, Object> param); // 게시판 입력
	public int updateBbsMstInfo(HashMap<String, Object> param); // 게시판 수정
} 
