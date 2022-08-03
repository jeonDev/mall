package com.shop.mall.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shop.mall.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ProductController {
	
	@Autowired
	private ProductService service;
	
	/*
	 * 상품 입력
	 *  product_name, product_detail, price, product_state, product_type
	 *  product_list : [{product_size, product_cnt} ... ]
	 *  파일(선택)
	 * */
	@PostMapping(value="/admin/product/create")
	public ResponseEntity<HashMap<String, Object>> createProduct(@RequestPart HashMap<String, Object> param
			, @RequestPart(required = false) MultipartFile[] files) throws Exception {
		return new ResponseEntity<> (service.createProduct(param, files),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 사이즈 입력
	 * */
	@PostMapping(value = "/admin/product/list/create")
	public ResponseEntity<HashMap<String, Object>> createProductList(@RequestBody List<HashMap<String, Object>> params) 
			throws Exception {
		return new ResponseEntity<> (service.insertProductList(params),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 사이즈 수정
	 * */
	@PostMapping(value = "/admin/product/list/update")
	public ResponseEntity<HashMap<String, Object>> updateProductList(@RequestBody HashMap<String, Object> param) 
			throws Exception {
		return new ResponseEntity<> (service.updateProductList(param),
				HttpStatus.OK);
	}
	
	
	/*
	 * 상품 목록 조회
	 * param : 	curPage (현재 페이지)
	 * 			pageUnit (페이지에 출력 건 수 단위)
	 * 			blockUnit (한 페이지에 보여질 페이지 수)
	 * 			param (조회조건)
	 * */
	@GetMapping(value="/product/list")
	public ResponseEntity<HashMap<String, Object>> selectProductList(@RequestParam(defaultValue="1") int curPage
			, @RequestParam(defaultValue="10") int pageUnit
			, @RequestParam(defaultValue="5") int blockUnit
			, @RequestParam HashMap<String, Object> param) throws Exception{
		
		return new ResponseEntity<> (service.selectProductList(curPage, pageUnit, blockUnit, param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 사이즈 조회
	 * */
	@GetMapping(value="/admin/product/size/list")
	public ResponseEntity<HashMap<String, Object>> selectProductSizeList(@RequestParam HashMap<String, Object> param) throws Exception {
		return new ResponseEntity<> (service.selectProductSizeList(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 조회수 update
	 * */
	@PostMapping(value="/product/update/view/{productId}")
	public ResponseEntity<HashMap<String, Object>> updateProductView(@PathVariable("productId") String productId) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		param.put("product_id", productId);
		param.put("product_view","1");
		
		result.put("result", service.updateProductMst(param));
		return new ResponseEntity<> (result, HttpStatus.OK);
	}
	
	/*
	 * 상품 수정
	 * product_id
	 * product_list :[{product_no, product_cnt, product_size} ...]
	 * file_list : [{file_no} ...]
	 * */
	@PostMapping(value="/admin/product/update/{productId}")
	public ResponseEntity<HashMap<String, Object>> updateProduct(@RequestPart HashMap<String, Object> param
			, @RequestPart(required = false) MultipartFile[] files
			, @PathVariable(name = "productId") String productId) throws Exception {
		param.put("product_id", productId);
		return new ResponseEntity<> (service.updateProductInfo(param, files),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 상세 조회
	 * */
	@GetMapping(value="/product/{productId}")
	public ResponseEntity<HashMap<String, Object>> selectProductOne(@PathVariable("productId") String productId) {
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("product_id", productId);
		return new ResponseEntity<> (service.selectProductOne(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 리뷰 조회
	 *  PR001 : 일반 후기
	 *  PR002 : 상품 사진 후기
	 *  PR003 : 익명후기
	 * */
	@GetMapping(value="/product/review/list")
	public ResponseEntity<HashMap<String, Object>> selectProductReviewList(@RequestParam(defaultValue="1") int curPage
			, @RequestParam(defaultValue="10") int pageUnit
			, @RequestParam(defaultValue="5") int blockUnit
			, @RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.selectProductReviewList(curPage, pageUnit, blockUnit, param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 리뷰 조회(단일건)
	 * */
	@GetMapping(value = "/product/review/detail")
	public ResponseEntity<HashMap<String, Object>> selectProductReviewDetail(@RequestParam HashMap<String, Object> param) 
			throws Exception {
		return new ResponseEntity<> (service.selectProductReviewDetail(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 리뷰 작성
	 * */
	@PostMapping(value="/user/product/review/create")
	public ResponseEntity<HashMap<String, Object>> insertProductReview(@RequestPart(name = "param") HashMap<String, Object> param
			, @RequestPart(name= "files", required = false) MultipartFile[] files) throws IOException, Exception {
		
		return new ResponseEntity<> (service.insertProductReview(param, files),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 리뷰 삭제
	 * */
	@PostMapping(value="/product/review/delete/{review_no}")
	public HashMap<String, Object> deleteProductReview(@PathVariable("review_no") String reviewNo) {
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		param.put("review_no", reviewNo);
		
		return service.deleteProductReview(param);
	}
	
	/*
	 * 좋아요 눌렀는지 체크
	 * */
	@GetMapping(value="/user/product/review/chk/{review_no}")
	public ResponseEntity<HashMap<String, Object>> productReviewChk(@PathVariable("review_no") String reviewNo
			, HashMap<String, Object> param){
		param.put("review_no", reviewNo);
		
		return new ResponseEntity<> (service.selectReviewStateChk(param)
				, HttpStatus.OK);
	}
	
	/*
	 * 상품 좋아요
	 * */
	@PostMapping(value="/user/product/review/insert/{review_no}")
	public ResponseEntity<HashMap<String, Object>> productReviewUpdate(@PathVariable("review_no") String reviewNo
			, HashMap<String, Object> param){
		param.put("review_no", reviewNo);
		
		return new ResponseEntity<> (service.productReviewInsert(param)
				, HttpStatus.OK);
	}
	
	/*
	 * 상품 좋아요 취소
	 * */
	@PostMapping(value="/user/product/review/delete/{review_no}")
	public ResponseEntity<HashMap<String, Object>> productReviewDelete(@PathVariable("review_no") String reviewNo
			, HashMap<String, Object> param){
		param.put("review_no", reviewNo);
		
		return new ResponseEntity<> (service.productReviewDelete(param)
				, HttpStatus.OK);
	}
	
	/*
	 * 상품 장바구니 추가
	 * */
	@PostMapping(value="/user/product/basket/add")
	public ResponseEntity<HashMap<String, Object>> addProductToBasket(@RequestBody List<HashMap<String, Object>> param) {
		
		return new ResponseEntity<> (service.addProductToBasket(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 장바구니 제거
	 * */
	@PostMapping(value="/user/product/basket/delete")
	public ResponseEntity<HashMap<String, Object>> deleteProductBasket(@RequestBody List<String> params) {
		
		return new ResponseEntity<> (service.deleteProductBasket(params),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 장바구니 수정 (갯수 수정)
	 * */
	@PostMapping(value="/user/product/basket/update")
	public ResponseEntity<HashMap<String, Object>> updateProductBasket(@RequestBody HashMap<String, Object> param) {
		
		return new ResponseEntity<> (service.updateProductBasket(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 장바구니 조회
	 * */
	@GetMapping(value="/user/product/basket")
	public ResponseEntity<HashMap<String, Object>> selectProductBasket() {
		
		return new ResponseEntity<> (service.selectProductBasket(),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 카테고리 조회
	 * */
	@GetMapping(value="/product/category/list")
	public ResponseEntity<HashMap<String, Object>> getCategoryList(@RequestParam HashMap<String, Object> param){
		return new ResponseEntity<> (service.getCategoryList(param),
				HttpStatus.OK);
	}
	
	/*
	 * 상품 판매량 통계
	 * */
	@GetMapping(value="/admin/product/sales/cnt")
	public ResponseEntity<HashMap<String, Object>> selectProductSalesCnt() {
		return new ResponseEntity<> (service.selectProductSalesCnt(), HttpStatus.OK);
	}
	
	/*
	 * 상품 재고 체크
	 * */
	@PostMapping(value="/product/stock/check")
	public ResponseEntity<HashMap<String ,Object>> chkProductStock(@RequestBody List<HashMap<String, Object>> param) 
			throws Exception{
		
		return new ResponseEntity<> (service.chkProductStock(param), HttpStatus.OK);
	}
	
	/*
	 * 상품 등록 (Excel)
	 * */
	@PostMapping(value="/admin/product/create/excel")
	public ResponseEntity<HashMap<String, Object>> createProductExcel(@RequestPart(required = false) MultipartFile file) 
			throws ParseException, Exception{
		
		return new ResponseEntity<> (service.createProductExcel(file), HttpStatus.OK);
	}

}
