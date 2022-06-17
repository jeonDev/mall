package com.shop.mall.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;
import com.shop.mall.config.security.JwtTokenProvider;
import com.shop.mall.model.CmmnUser;
import com.shop.mall.repository.UserDao;
import com.shop.mall.util.StringUtil;
import com.shop.mall.util.Utils;

import jdk.internal.org.jline.utils.Log;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final PasswordEncoder encoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final Utils util;
	
	@Autowired
	private UserDao dao;
	
	/*
	 * 회원가입
	 * */
	public ResponseEntity<HashMap<String, Object>> insertUserJoin(HashMap<String, Object> user) 
			throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		String role		= StringUtil.nullToBlank((String) user.get("role"));
		String password = StringUtil.nullToBlank((String) user.get("password"));
		String id		= StringUtil.nullToBlank((String) user.get("id"));
		
		CmmnUser cmmnUser = dao.selectUserName(id);
		
		if(cmmnUser != null) {
			throw new DuplicationException("이미 사용중인 아이디입니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		if(role == null || role.equals("")) {
			user.put("role", "ROLE_USER");
		}
		password = encoder.encode(password);
		
		user.put("password", password);
		
		int userJoinResult = dao.insertUserJoin(user);
		
		if(userJoinResult > 0) {
			result.put("code", "0000");
			result.put("msg" , "회원가입에 성공하였습니다.");
		} else {
			result.put("code", "9999");
			result.put("msg" , "회원가입에 실패하였습니다.");
		}
		return new ResponseEntity<> (result, HttpStatus.OK);
	}
	
	/*
	 * 로그인
	 * */
	public ResponseEntity<HashMap<String, Object>> login(HashMap<String, Object> user)
			throws IllegalAccessException {
		HashMap<String, Object> tokenResult = new HashMap<String, Object>();
		
		CmmnUser member = dao.selectUserLogin(user);
		if(member == null) throw new IllegalAccessException("가입되지 않은 계정입니다.");

		if(!encoder.matches(user.get("password").toString(), member.getPassword())) {
			throw new IllegalArgumentException("잘못된 비밀번호입니다.");
		}
		
		// 권한
		String [] rolesString = member.getRole().split(" ");
		ArrayList<String> roles = new ArrayList<String>();
		
		for(String role : rolesString) {
			roles.add(role);
		}
		
		member.setRoles(roles);
		
		String accessToken = jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getUsername());
		
		tokenResult.put("accessToken" , accessToken);	// Access Token : X-AUTH-TOKEN으로 localStorage에 저장.
		tokenResult.put("code", "0000");	// 로그인 성공
		tokenResult.put("message" , "로그인에 성공하였습니다.");
		
		ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.maxAge(60*60*24*7)
				.build();
		
		// Refresh Token DB 저장
		HashMap<String, Object> loginParam = new HashMap<String, Object>();
		loginParam.put("id",  StringUtil.nullToBlank(member.getId()));
		loginParam.put("refreshToken", StringUtil.nullToBlank(refreshToken));
		
		dao.setRefreshToken(loginParam);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(tokenResult);
	}
	
	public ResponseEntity<HashMap<String, Object>> reTokenCreate(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String, Object> result = new HashMap<String, Object>();
		String extToken = jwtTokenProvider.resolveToken(request);
		String refreshToken = jwtTokenProvider.getRefreshToken(request);
		String accessToken = "";
		logger.info("refresh Token : " + refreshToken);
		
		if(!"".equals(extToken)) {
			if(!refreshToken.equals("") && 
					jwtTokenProvider.validateRefreshToken(refreshToken)) {
				param.put("refreshToken", refreshToken);
				CmmnUser member = dao.selectUserLogin(param);
				
				ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
						.httpOnly(true)
						.maxAge(60*60*24*7)
						.build();
				
				if(member != null) {
					// 권한
					String [] rolesString = member.getRole().split(" ");
					ArrayList<String> roles = new ArrayList<String>();
					
					for(String role : rolesString) {
						roles.add(role);
					}
					
					member.setRoles(roles);
					
					accessToken = jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
					refreshToken = jwtTokenProvider.createRefreshToken(member.getUsername());
					
					result.put("accessToken" , accessToken);	// Access Token : X-AUTH-TOKEN으로 localStorage에 저장.
					result.put("code", "0000");					// 로그인 성공
					
					// Refresh Token DB 저장
					HashMap<String, Object> loginParam = new HashMap<String, Object>();
					loginParam.put("id",  StringUtil.nullToBlank(member.getId()));
					loginParam.put("refreshToken", StringUtil.nullToBlank(refreshToken));
					
					//jwtTokenProvider.setRefreshToken(loginParam);
				}
				
				return ResponseEntity.ok()
						//.header(HttpHeaders.SET_COOKIE, cookie.toString())
						.body(result);
			} else {
				
				Log.info("Unauthorized!! (Not Refresh Token)");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요한 기능입니다.");
				return ResponseEntity.ok()
						.body(result);
			}
		} else {
			return null;
		}
		
	}
	
	/* 로그아웃
	 * 
	 */
    public ResponseEntity<HashMap<String, Object>> logout(HttpServletRequest request) 
    		throws Exception{
    	// Access Token 제거 -> localStorage
    	
    	// Refresh Token 제거 -> HTTP Cookie 및 DB(CMMN_USER.REFRESH_TOKEN)
    	String accessToken = jwtTokenProvider.resolveToken(request);
    	
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
        	throw new DuplicationException("잘못된 접근입니다.", ErrorCode.INTER_SERVER_ERROR);
        }

        // 2. 해당 Access Token 유효시간 초기화
        
        
        // 3. Refresh Token 제거
        ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
				.httpOnly(true)
				.maxAge(0)
				.build();
    	
        HashMap<String, Object> logoutParam = new HashMap<String, Object>();
        
        CmmnUser user = util.tokenToUserInfo();
		
        String id = (String) user.getId();
        
        logoutParam.put("id", id);
        logoutParam.put("refreshToken", "");
        
        dao.setRefreshToken(logoutParam);
        
        return ResponseEntity.ok()
        		.header(HttpHeaders.SET_COOKIE, cookie.toString())
        		.body(null);
    }
    
    /*
     * 사용자 정보 조회
     * */
    public HashMap<String, Object> selectUserInfo(){
    	HashMap<String, Object> param = new HashMap<String, Object>();
    	HashMap<String, Object> result = new HashMap<String, Object>();
    	
    	// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		result.put("userInfo", dao.selectUserInfo(param));
		
		return result;
    }
    
    /*
     * 사용자 정보 수정
     * */
    public HashMap<String, Object> updateCmmnUser(HashMap<String, Object> param) 
    		throws Exception{
    	HashMap<String, Object> result = new HashMap<String, Object>();
    	
    	// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
    	
		String password = StringUtil.nullToBlank( (String) param.get("password") );
		
		if(!"".equals(password)) {
			password = encoder.encode(password);
			
			param.put("password", password);
		}
		
    	int upt = dao.updateCmmnUser(param);
    	
    	if(upt > 0) {
    		result.put("message", "정보 수정이 완료되었습니다.");
    	} else {
    		throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
    	}
    	return result;
    }
    
}
