package com.shop.mall.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.shop.mall.service.BoardService;

@RestController
public class BoardController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BoardService service;
	
	@GetMapping(value="/board/list")
	public ResponseEntity<HashMap<String, Object>> selectBbsMstList(@RequestParam(defaultValue="1") int curPage
			, @RequestParam(defaultValue="10") int pageUnit
			, @RequestParam(defaultValue="5") int blockUnit
			, @RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.selectBbsMstList(curPage, pageUnit, blockUnit, param),
				HttpStatus.OK);
	}
	
	@GetMapping(value="/board/{bbsNo}")
	public ResponseEntity<HashMap<String, Object>> selectBbsMstOne(@PathVariable("bbsNo") String bbsNo,
			@RequestParam HashMap<String, Object> param){
		param.put("bbs_no", bbsNo);
		
		return new ResponseEntity<> (service.selectBbsMstOne(param),
				HttpStatus.OK);
	}
	/*
	 * 게시글 입력
	 * param : bbs_type(게시글 종류) / title(제목) / content(내용) / wrt_state(작성 상태)
	 * */
	@PostMapping(value="/user/board/create")
	public ResponseEntity<HashMap<String, Object>> insertBoard(@RequestBody HashMap<String, Object> param) 
			throws Exception{
		
		return new ResponseEntity<> (service.insertBoard(param),
				HttpStatus.OK);
	}
	/*
	 * 게시글 수정
	 * */
	@PostMapping(value="/user/board/update")
	public ResponseEntity<HashMap<String, Object>> updateBbsMst(@RequestBody HashMap<String, Object> param) 
			throws Exception{
		return new ResponseEntity<> (service.updateBbsMstInfo(param),
				HttpStatus.OK);
	}
	
	/*
	 * 게시글 수정
	 * */
	@PostMapping(value="/board/update")
	public ResponseEntity<HashMap<String, Object>> updateBbsMstViewCnt(@RequestBody HashMap<String, Object> param) 
			throws Exception{
		HashMap<String, Object> params= new HashMap<String, Object>();
		params.put("view_cnt", param.get("view_cnt"));
		params.put("bbs_no", param.get("bbs_no"));
		return new ResponseEntity<> (service.updateBbsMstInfo(params),
				HttpStatus.OK);
	}
}
