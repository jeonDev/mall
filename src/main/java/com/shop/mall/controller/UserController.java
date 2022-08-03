package com.shop.mall.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.mall.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {

	@Autowired
	private UserService service;
	
	/*
	 * 회원가입
	 * Parameter : 
	 * 필수 : id, password, name, tel, email
	 * 선택 : role
	 * */
	@PostMapping("/join")
	public ResponseEntity<HashMap<String, Object>> join(@RequestParam HashMap<String, Object> user) 
			throws Exception{
		return service.insertUserJoin(user);
	}
	
	/*
	 * 로그인
	 * Paremeter : 
	 * 필수 : id / password
	 * */
	@PostMapping("/login")
	public ResponseEntity<HashMap<String, Object>> login(@RequestBody HashMap<String, Object> user)
			throws IllegalAccessException {
		return service.login(user);
	}
	
	/*
	 * 로그아웃
	 * */
	@PostMapping("/logout")
	public ResponseEntity<HashMap<String, Object>> logout(HttpServletRequest request) 
			throws Exception{
		return service.logout(request);
	}
	
	/*
	 * Token 재 발급
	 * */
	@PostMapping("/refresh/token")
	public ResponseEntity<HashMap<String, Object>> reTokenCreate(HttpServletRequest request, HttpServletResponse response) 
			throws IOException{
		return service.reTokenCreate(request, response);
	}
	
	@GetMapping("/user/get/info")
	public ResponseEntity<HashMap<String, Object>> selectUserInfo(){
		return new ResponseEntity<> (service.selectUserInfo(),
				HttpStatus.OK);
	}
	
	/*
	 * 사용자 정보 수정
	 * */
	@PostMapping("/user/info/update")
	public ResponseEntity<HashMap<String, Object>> updateCmmnUser(@RequestBody HashMap<String, Object> param) 
			throws Exception{
		return new ResponseEntity<> (service.updateCmmnUser(param),
				HttpStatus.OK);
	}
	
	/*
	 * 사용자 정보 삭제
	 * */
	@PostMapping("/user/info/delete")
	public ResponseEntity<HashMap<String, Object>> deleteCmmnUser() 
			throws Exception{
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("del_yn", "Y");
		
		return new ResponseEntity<> (service.updateCmmnUser(param),
				HttpStatus.OK);
	}
}
