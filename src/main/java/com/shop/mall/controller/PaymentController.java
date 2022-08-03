package com.shop.mall.controller;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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

import com.shop.mall.service.PaymentService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PaymentController {

	@Autowired
	private PaymentService service;
	
	/*
	 * 상품 구매내역 조회
	 * 		- 장바구니 페이지에서 올 경우 : (LIST) BASKET_NO
	 * 		- 구매하기에서 올 경우 		: (LIST Map) {PRODUCT_NO, PRODUCT_CNT}
	 * Return : 상품 정보
	 * */
	@GetMapping("/pay/info/{payInfo}/list")
	public ResponseEntity<HashMap<String, Object>> getProductPayInfoList(@RequestParam HashMap<String, Object> param,
			@PathVariable(value = "payInfo") String payInfo) {
		return new ResponseEntity<> (service.getProductPayInfoList(param, payInfo), HttpStatus.OK);
	}
	
	/*
	 * 상품 결제 처리
	 * */
	@PostMapping(value = "/user/payment")
	public ResponseEntity<HashMap<String, Object>> productPayment(@RequestBody HashMap<String, Object> param) 
			throws Exception {
		log.info("Controller ==> " + param);
		return new ResponseEntity<> (service.productPayment(param), 
				HttpStatus.OK);
	}
	
	/*
	 * 상품 결제 콜백
	 * */
	@PostMapping(value = "/user/payment/callback")
	public ResponseEntity<HashMap<String, Object>> productPaymentCallback(@RequestBody HashMap<String, Object> param) 
			throws SQLIntegrityConstraintViolationException {
		log.info("Controller ==> " + param);
		return new ResponseEntity<> (service.productPaymentCallback(param), 
				HttpStatus.OK);
	}
	
	/*
	 * 주문내역 조회
	 * */
	@GetMapping(value = "/user/payment/order/list")
	public ResponseEntity<HashMap<String, Object>> selectOrderInfoList(@RequestParam(defaultValue="1") int curPage
			, @RequestParam(defaultValue="10") int pageUnit
			, @RequestParam(defaultValue="5") int blockUnit
			, @RequestParam HashMap<String, Object> param) throws Exception{
		return new ResponseEntity<> (service.selectOrderInfoList(curPage, pageUnit, blockUnit, param)
				, HttpStatus.OK);
	}
	
	/*
	 * 주문상태별 수량 조회
	 * */
	@GetMapping(value = "/user/payment/state/cnt")
	public ResponseEntity<HashMap<String, Object>> selectOrderPaymentStateCnt() 
			throws Exception {
		return new ResponseEntity<> (service.selectOrderPaymentStateCnt(), HttpStatus.OK);
	}
	
	/*
	 * 구매확정 처리
	 * */
	@PostMapping(value="/user/payment/update")
	public ResponseEntity<HashMap<String, Object>> updatePaymentState(@RequestBody HashMap<String, Object> param) 
			throws Exception{
		return new ResponseEntity<> (service.updatePaymentState(param),
				HttpStatus.OK);
	}
	
	
	
	/*
	 * 사용자 주문내역 조회
	 * */
	@GetMapping(value="/user/product/order/list")
	public @ResponseBody List<HashMap<String, Object>> productOrderList(){
		
		return service.productOrderList();
	}
	
	/*
	 * 주문내역 조회 (나중에 비회원 추가 시, 필요!)
	 * */
	@GetMapping(value="/user/product/order/list/{order_no}")
	public @ResponseBody List<HashMap<String, Object>> productOrderList(@PathVariable(value = "order_no") String orderNo){
		
		return service.productOrderByNoList(orderNo);
	}
	
	/*
	 * 주문 상태 내역 조회
	 * */
	@GetMapping(value="/payment/state/list/{payment_no}")
	public @ResponseBody List<HashMap<String, Object>> selectPaymentStateList(@PathVariable(value="payment_no") String paymentNo){
		
		return service.selectPaymentStateList(paymentNo);
	}
	
	@PostMapping(value="/excel")
	public void excel(HttpServletResponse response, 
			@RequestBody HashMap<String, Object> excel) throws IOException{
		
		service.excel(response, excel);
	}
}
