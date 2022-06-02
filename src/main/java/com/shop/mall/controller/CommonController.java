package com.shop.mall.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shop.mall.service.CommonService;

@RestController
public class CommonController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CommonService service;
	
	/*
	 * 메뉴 조회
	 * */
	@GetMapping(value="/get/menu/list")
	public ResponseEntity<List<HashMap<String, Object>>> getMenuList(@RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.selectCmmnMenuList(param),
				HttpStatus.OK);
	}
	
	/*
	 * 공통코드 조회
	 * param : cmn_type
	 * */
	@GetMapping(value="/get/code/list")
	public ResponseEntity<List<HashMap<String, Object>>> getCmmnCodeList(@RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.selectCmmnCodeList(param),
				HttpStatus.OK);
	}
	
	/*
	 * 인증번호 요청
	 * */
	@GetMapping(value="/req/cert")
	public ResponseEntity<HashMap<String, Object>> createCertNum(@RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.createCertNum(param),
				HttpStatus.OK);
	}
	
	/*
	 * 인증번호 확인
	 * */
	@GetMapping(value="/cert/check")
	public ResponseEntity<HashMap<String, Object>> certNumCheck(@RequestParam HashMap<String, Object> param) 
			throws Exception{
		logger.info("" + param);
		return new ResponseEntity<> (service.certNumCheck(param),
				HttpStatus.OK);
	}
	
	/*
	 * 파일 업로드
	 * */
	@PostMapping(value="/upload/file")
	public ResponseEntity<HashMap<String, Object>> uploadFile(@RequestPart HashMap<String, Object> param
			, @RequestPart MultipartFile[] files) throws IOException {
		System.out.println("==================================");
		System.out.println("==================================");
		System.out.println("==================================");
		return new ResponseEntity<> (service.uploadFile(param, files),
				HttpStatus.OK);
	}
	
}
