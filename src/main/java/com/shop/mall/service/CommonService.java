package com.shop.mall.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;
import com.shop.mall.model.CmmnUser;
import com.shop.mall.repository.CommonDao;
import com.shop.mall.util.StringUtil;
import com.shop.mall.util.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {
	
	@Autowired
	private CommonDao dao;
	
	@Value(value="${upload-file-path}")
	private String uploadFilePath;
	
	@Value(value="${upload-path}")
	private String uploadPath;
	
	private final Utils util;
	
	/*
	 * 공통 파일 INSERT
	 * */
	public int insertCmmnFile(HashMap<String, Object> param) {
		return dao.insertCmmnFile(param);
	}
	
	/*
	 * 공통 코드 조회 
	 * param : cmn_type
	 * */
	public List<HashMap<String, Object>> selectCmmnCodeList(HashMap<String, Object> param){
		List<HashMap<String, Object>> result = dao.selectCmmnCodeList(param);
		
		return result;
	}
	
	/*
	 * 공통 메뉴 조회
	 * */
	public List<HashMap<String, Object>> selectCmmnMenuList(HashMap<String, Object> param){
		// User Roles Get
		CmmnUser user = util.tokenToUserInfo();
		
		if(!user.getRoles().isEmpty()) {
			param.put("roles", user.getRoles());
		} else {
			List<String> roles = new ArrayList<String>();
			
			roles.add("ROLE_USER");
			
			param.put("roles", roles);
		}
		
		// 로그인 여부 체크
		String loginMenu = (String) param.get("menu_div");

		if("LOGIN_MENU".equals(loginMenu)) {
			
			if(user.getId() != null) {
				param.put("menu_div", "LOGOUT_MENU");
			}
		}
		
		List<HashMap<String, Object>> result = dao.selectCmmnMenuList(param);
		
		return result;
	}
	
	/*
	 * 인증번호 요청
	 * */
	public HashMap<String, Object> createCertNum(HashMap<String, Object> param){
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int certNumber = (int) (Math.random() * 8999 + 1000);
		
		param.put("cert_num", certNumber);
		
		int certResult = dao.createCertNum(param);
		
		if(certResult > 0) {
			String certSeq = (String) param.get("cert_seq");
			
			result.put("code"	, "0000");
			result.put("msg"	, "인증번호 요청에 성공하였습니다.");
			result.put("certSeq", certSeq);
			
			String certNum = StringUtil.nullToBlank(param.get("cert_num").toString());
			result.put("certNum", certNum);
			
		} else {
			result.put("code"	, "9999");
			result.put("msg"	, "인증번호 요청에 실패하였습니다.");
		}
		
		return result;
	}
	
	/*
	 * 인증번호 확인
	 * */
	public HashMap<String, Object> certNumCheck(HashMap<String, Object> param) 
			throws Exception {
		String certSeq = StringUtil.nullToBlank((String) param.get("cert_seq"));
		String certNum = StringUtil.nullToBlank((String) param.get("cert_num"));
		if(certNum.equals("")) {
			throw new DuplicationException("인증번호를 입력해주세요.", ErrorCode.INTER_SERVER_ERROR);
		} else if(certSeq.equals("")) {
			throw new DuplicationException("인증번호를 요청해주세요.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<String, Object> checkCertInfo = dao.certNumCheck(param);
		
		String certCheckYn = (String)checkCertInfo.get("CERT_CHECK_YN");
		String certTermYn  = (String)checkCertInfo.get("CERT_TERM_YN");
		
		if("Y".equals(certTermYn)) {
			if("Y".equals(certCheckYn)) {
				result.put("code"	, "0000");
				result.put("msg"	, "인증번호 인증에 성공하였습니다.");
				result.put("certYn"	, "Y");
			} else {
				throw new DuplicationException("인증번호 인증에 실패하였습니다.", ErrorCode.INTER_SERVER_ERROR);
			}
		} else {
			throw new DuplicationException("인증번호 유효기간이 지났습니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 파일 업로드
	 * */
	public HashMap<String, Object> uploadFile(HashMap<String, Object> param, 
			MultipartFile[] files) throws IOException {
		HashMap<String, Object> result = new HashMap<String, Object>();
		int insFile = 0;
		
		// User Roles Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(files != null && files.length > 0) {
			HashMap<String, Object> fileParam = new HashMap<String, Object>();
			String[] ext	= {"image/png", "image/jpg", "image/jpeg", "image/gif"};
			String filePath = uploadPath + uploadFilePath;
			int srtOdr = 1;
			
			String fileDiv = StringUtil.nullToBlank((String) param.get("file_div"));
			String fileRef = StringUtil.nullToBlank((String) param.get("file_ref"));
			
			fileParam.put("file_div", fileDiv);
			fileParam.put("file_ref", fileRef);
			fileParam.put("id"		  , id);

			for(MultipartFile file: files) {
				if(!file.isEmpty()) {
					String fileName = util.uploadFile(file, filePath, ext);
					
					fileParam.put("file_src"	, uploadFilePath);
					fileParam.put("file_real_nm", fileName);
					fileParam.put("file_nm"		, file.getOriginalFilename());
					fileParam.put("srt_odr" 	, srtOdr++);

					// 상품 이미지 INSERT
					insFile += this.insertCmmnFile(fileParam);
				}
			}
		}
		
		result.put("success", insFile);
		result.put("message", "파일이 정상적으로 업로드 되었습니다.");
		
		return result;
	}
	
	/*
	 * Excel Download
	 * */
	public void excelDownload(HttpServletResponse response,
			HashMap<String, Object> excelData) throws IOException{
		
		Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("첫번째 시트");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;
        System.out.println("========================");
        System.out.println(excelData);
        System.out.println("========================");
        List<String> headerData = (List<String>) excelData.get("header");

        // Header
        row = sheet.createRow(rowNum++);
        int headerLine = 0;
        for(String header : headerData) {
        	cell = row.createCell(headerLine++);
        	cell.setCellValue(header);
        }

        List<HashMap<String, Object>> bodyData = (List<HashMap<String, Object>>) excelData.get("body");
        System.out.println(bodyData);
        
        // Body
        for(HashMap<String, Object> bodyMap : bodyData) {
        	row = sheet.createRow(rowNum++);
        	
        	int bodyLine = 0;
        	for(Object key : bodyMap.keySet()){
        		String body = StringUtil.nullToBlank(String.valueOf(bodyMap.get(key)));
        		cell = row.createCell(bodyLine++);
        		cell.setCellValue(body);
        	}
        }

        String filename = StringUtil.nullToBlank((String) excelData.get("filename"));
        
        // 컨텐츠 타입과 파일명 지정
        response.setContentType("ms-vnd/excel");
//        response.setHeader("Content-Disposition", "attachment;filename=example.xls");
        response.setHeader("Content-Disposition", "attachment;filename=" + (filename.equals("") ? "excel" : filename) + ".xlsx");

        // Excel File Output
        wb.write(response.getOutputStream());
        wb.close();
	}
}
