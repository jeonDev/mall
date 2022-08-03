package com.shop.mall.config;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.shop.mall.model.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

	@ExceptionHandler(DuplicationException.class)
	public ResponseEntity<Object> handleDuplicationException(DuplicationException ex){
		log.info(ex.getClass().getName());
		log.error("error", ex);
		ErrorResponse response = new ErrorResponse(ex.getErrorCode()); 
		
		return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getErrorCode().getStatus()));
	}
	
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<Object> handleNullException(final Exception ex) {
		log.info(ex.getClass().getName());
		log.error("error", ex);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("message", "서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다.");
		return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	@ExceptionHandler({
		SQLIntegrityConstraintViolationException.class
	})
	public ResponseEntity<Object> cntChkAll(final Exception ex){
		log.info(ex.getClass().getName());
		log.error("error", ex);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("message", "서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다.");
		return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler({
		SQLException.class
	})
	public ResponseEntity<Object> sqlChkAll(final Exception ex){
		log.info(ex.getClass().getName());
		log.error("error", ex);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("message", "서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다.");
		return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler({
		MaxUploadSizeExceededException.class
	})
	public ResponseEntity<Object> uploadFileSizeAll(final Exception ex){
		log.info(ex.getClass().getName());
		log.error("error", ex);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("message", "등록 가능한 파일의 용량을 초과하였습니다.");
		return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	// 500 error
	/*
	@ExceptionHandler({
		Exception.class
	})
	public ResponseEntity<Object> handleAll(final Exception ex){
		log.info(ex.getClass().getName());
		log.error("error", ex);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("message", ex.getMessage());
		return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
		//"서버 요청 시 에러가 발생하였습니다.\n관리자에게 문의바랍니다."
	}
	*/
	
}
