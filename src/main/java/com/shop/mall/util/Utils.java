package com.shop.mall.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.shop.mall.config.security.JwtTokenProvider;
import com.shop.mall.model.CmmnUser;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
@Component
public class Utils {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final JwtTokenProvider jwtTokenProvider;
	
	/*
	 * 로그인 사용자 정보 추출
	 * */
	public CmmnUser tokenToUserInfo() {
		
		CmmnUser user = jwtTokenProvider.getUserInfo();
		
		return user != null ? user : new CmmnUser();
	}
	
	/*
	 * Token에서 Id 추출
	 * */
	public String tokenToUserId(String token) {
		
		return jwtTokenProvider.getUserPk(token);
	}
	
	/*
	 * String -> JSONArray
	 * */
	public JSONArray strToJsonArray(String listStr) {
		
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonObj = new JSONArray();
		
		try {
			Object obj = jsonParser.parse(listStr);
			jsonObj = (JSONArray) obj;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return jsonObj;
	}

	
	/*
	 * 파일 업로드
	 * param : MultipartFile / 파일 경로 / 확장자 제한
	 * */
	public String uploadFile(MultipartFile file, String filePath, String[] exts) 
			throws IOException{
		
		UUID uuid 			= UUID.randomUUID();
		String fileName 	= file.getOriginalFilename();
		String realName		= uuid.toString() + "_" + fileName.replaceAll(" ", "");
		String contentType	= file.getContentType();
		String absolutePath = new File("").getAbsolutePath() + "//";
		int extSize 		= 0;
		
		for(String type : exts) {
			if(contentType.contains(type)) {
				extSize++;
			}
		}
		
		if(extSize < 1) {
			throw new IOException(contentType + " 확장자는 업로드 할 수 없습니다.");
		}
		
		File dest = new File(filePath);
		
		// 디렉토리가 없으면 생성
		if(!dest.exists()) {
			dest.mkdirs();
		}
		
		dest = new File(absolutePath + filePath + "/" + realName);
		
		file.transferTo(dest);
		
		return realName;
	}
	
	public JSONObject jsonToStrList(HashMap<String, Object> listStr) {
		
		logger.info("jsonToStrList");
		JSONObject jsonObj = new JSONObject(listStr); 
		/*JSONParser jsonParser = new JSONParser();
		JSONArray jsonObj = new JSONArray();
		
		try {
			Object obj = jsonParser.parse(listStr);
			jsonObj = (JSONArray) obj;
		} catch (ParseException e) {
			e.printStackTrace();
		}*/
		logger.info("" + jsonObj);
		return jsonObj;
	}
	
	/*
	 * Excel Download
	 * excel : {
	 * 		"excel_header" : ["1", "2" ...]
	 * 		"excel_body"   : [["1", "2", "3"] , [...], ...]
	 * 			}
	 * */
	public int excelDownload(HttpServletResponse response,
			HashMap<String, Object> excel) throws IOException{
		logger.info("" + excel);
		
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("첫번째 시트");
		Row row 	= null;
		Cell cell 	= null;
		int rowNum 	= 0;
		int cellNum = 0;
		
		// Header
		List<String> excelHeaders  = (List<String>) excel.get("excel_header");
		Iterator<String> headerObj = excelHeaders.iterator();
		
		row = sheet.createRow(rowNum++);
		while( headerObj.hasNext() ) {
			String c = headerObj.next();
			
			logger.info("Excel Header(" + cellNum + ") : " + c);
			cell = row.createCell(cellNum++);
			cell.setCellValue(c);
		}
		
		List<HashMap<String, Object>> excelBodys  = (List<HashMap<String, Object>>) excel.get("excel_body");
		Iterator<HashMap<String, Object>> bodyObj = excelBodys.iterator();
		
		while( bodyObj.hasNext() ) {
			HashMap<String, Object> map = bodyObj.next();
			Iterator<String> bodyMap 	= map.keySet().iterator();
			cellNum = 0;
			
			row = sheet.createRow(rowNum++);
			while( bodyMap.hasNext() ) {
				String key = bodyMap.next();
				String b   = (String) map.get(key);
				
				cell = row.createCell(cellNum++);
				cell.setCellValue(b);
			}
			
		}

		// 컨텐츠 타입과 파일명 지정
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");
		//response.setContentType("ms-vnd/excel");
		//response.setHeader("Content-Disposition", "attachment;filename=excel.xlsx");
		
		wb.write(response.getOutputStream());
		wb.close();
		
		return 0;
	}
}
