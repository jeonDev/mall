package com.shop.mall.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;
import com.shop.mall.model.CmmnUser;
import com.shop.mall.model.Paging;
import com.shop.mall.repository.BoardDao;
import com.shop.mall.util.Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	@Autowired
	private BoardDao dao;
	
	private final Utils util;
	
	/*
	 * 게시판 조회
	 * */
	public HashMap<String, Object> selectBbsMstList(int curPage, int pageUnit, int blockUnit, HashMap<String, Object> param){
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<HashMap<String, Object>> bbsList = null;
		
		int count = dao.selectBbsMstListCount(param);
		
		Paging paging = new Paging(count, curPage, pageUnit, blockUnit);
		
		param.put("start", paging.getStart());
		param.put("end", paging.getEnd());
		
		bbsList = dao.selectBbsMstList(param);
		
		result.put("bbsList", bbsList);
		result.put("page", paging);
		
		return result;
	}
	
	/*
	 * 
	 * */
	public HashMap<String, Object> selectBbsMstOne(HashMap<String, Object> param){
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		result.put("bbsInfo", dao.selectBbsMstOne(param));
		
		return result;
	}
	
	/*
	 * 게시판 작성
	 * */
	public HashMap<String, Object> insertBoard(HashMap<String, Object> param) 
			throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		int ins = 0;
		
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", (String) user.getId());
		}
		
		ins += dao.insertBoard(param);
		
		if(ins > 0) {
			result.put("message" , "게시글이 입력되었습니다.");
			result.put("bbsNo", param.get("bbs_no"));
		} else {
			throw new DuplicationException("서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 게시판 수정
	 * */
	public HashMap<String, Object> updateBbsMstInfo(HashMap<String, Object> param) 
			throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		int upt = 0;
		
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", (String) user.getId());
		}
		
		upt += dao.updateBbsMstInfo(param);
		
		if(upt > 0) {
			result.put("message" , "게시글이 수정되었습니다.");
			result.put("bbsNo", param.get("bbs_no"));
		} else {
			throw new DuplicationException("서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
}
