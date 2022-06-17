package com.shop.mall.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;
import com.shop.mall.model.CmmnUser;
import com.shop.mall.model.Paging;
import com.shop.mall.repository.ProductDao;
import com.shop.mall.util.StringUtil;
import com.shop.mall.util.Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ProductDao dao;
	
	@Autowired
	private CommonService commonService;
	
	@Value(value="${upload-file-path}")
	private String uploadFilePath;
	
	@Value(value="${upload-path}")
	private String uploadPath;
	
	private final Utils util;
	
	/*
	 * 상품 입력
	 * */
	@Transactional(rollbackFor = Exception.class)
	public HashMap<String, Object> createProduct(HashMap<String, Object> param
			, MultipartFile[] files) throws IOException, ParseException, Exception{
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		if(files.length > 5) {
			throw new IOException("파일은 5개 이상 등록할 수 없습니다.");
		}
		
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int resultMst	= this.insertProductMst(param);
		HashMap<String, Object> resultList = new HashMap<String, Object>();
		int resultFile	= 0;
		
		String productId = (String) param.get("product_id");
		
		// MST 테이블 등록 성공
		// List 테이블 등록
		if(resultMst > 0) {
			List<HashMap<String, Object>> listParam = new ArrayList<HashMap<String,Object>>();
			
			List<String> list = (List<String>) param.get("product_size");
			System.out.println(list);
			for(String productSize : list) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("product_size", productSize);
				map.put("product_id", productId);
				map.put("product_cnt", 0);
				map.put("price_discount", 0);
				listParam.add(map);
			}
			
			// PRODUCT_LIST INSERT
			resultList = insertProductList(listParam);
		} else {
			throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		// LIST 테이블 등록 성공
		// FILE 등록
		String listResult = (String) resultList.get("code");
		if( "0000".equals(listResult) ) {
			HashMap<String, Object> fileParam = new HashMap<String, Object>();
			String[] ext	= {"image/png", "image/jpg", "image/jpeg", "image/gif"};
			String filePath = uploadPath + uploadFilePath;
			int srtOdr = 1;
			
			fileParam.put("product_id", productId);
			fileParam.put("id"		  , id);

			for(MultipartFile file: files) {
				if(!file.isEmpty()) {
					String fileName = util.uploadFile(file, filePath, ext);
					
					fileParam.put("file_src"	, uploadFilePath);
					fileParam.put("file_real_nm", fileName);
					fileParam.put("file_nm"		, file.getOriginalFilename());
					fileParam.put("srt_odr" 	, srtOdr++);

					// 상품 이미지 INSERT
					resultFile += this.insertProductFile(fileParam);
				}
			}
		}
		
		result.put("message", "상품을 등록하였습니다.");
		result.put("productId", productId);
		
		return result;
	}
	
	/*
	 * 상품 수정
	 * 	PRODUCT_MST(UPDATE)
	 *  PRODUCT_FILE(UPDATE / INSERT / DELETE(DEL_YN : Y))
	 * */
	@Transactional(rollbackFor = Exception.class)
	public HashMap<String, Object> updateProductInfo(HashMap<String, Object> param
			, MultipartFile[] files) throws Exception{
		
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int mstUpdate  = 0;
		
		String productId = (String) param.get("product_id");
		
		// PRODUCT_MST Update
		mstUpdate += updateProductMst(param);
		
		// uploadFile : files
		// deleteFile : file_id (array)
		int delFile = 0;
		int insFile = 0;
		
		// Delete Files
		
		List<String> deleteFileList = (List<String>) param.get("file_id");
		
		for(String file : deleteFileList) {
			System.out.println(file);
			HashMap<String, Object> deleteFileMap = new HashMap<String, Object>();
			deleteFileMap.put("id", id);
			deleteFileMap.put("file_id", file);
			deleteFileMap.put("del_yn", "Y");
			System.out.println(deleteFileMap);
			delFile += this.updateProductFile(deleteFileMap);
		}
		
		// Upload Files
		if(files != null && files.length > 0) {
			HashMap<String, Object> fileParam = new HashMap<String, Object>();
			String[] ext	= {"image/png", "image/jpg", "image/jpeg", "image/gif"};
			String filePath = uploadPath + uploadFilePath;
			// int srtOdr = 1;
			
			fileParam.put("product_id", productId);
			fileParam.put("id"		  , id);

			for(MultipartFile file: files) {
				if(!file.isEmpty()) {
					String fileName = util.uploadFile(file, filePath, ext);
					
					fileParam.put("file_src"	, uploadFilePath);
					fileParam.put("file_real_nm", fileName);
					fileParam.put("file_nm"		, file.getOriginalFilename());
					//fileParam.put("srt_odr" 	, srtOdr++);

					// 상품 이미지 INSERT
					insFile += insertProductFile(fileParam);
				}
			}
		}
		
		result.put("productId", productId);
		result.put("message", "상품 정보가 변경되었습니다.");
		
		return result;
	}
	
	/*
	 * 상품 목록 조회
	 * */
	public HashMap<String, Object> selectProductList(int curPage, int pageUnit, int blockUnit, HashMap<String, Object> param) 
			throws Exception {
		
		int count = dao.selectProductListCount(param);
		Paging paging = new Paging(count, curPage, pageUnit, blockUnit);
		
		param.put("start", paging.getStart());
		param.put("end"	 , paging.getEnd());
		List<HashMap<String, Object>> productList = dao.selectProductList(param);
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("productList", productList);
		result.put("page", paging);
		
		return result;
	}
	
	/*
	 * 상품 사이즈 조회
	 * */
	public HashMap<String, Object> selectProductSizeList(HashMap<String, Object> param) 
			throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		result.put("productList", dao.selectProductSizeList(param));
		
		return result;
	}
	
	/*
	 * 상품 상세 조회
	 * 상품 정보 / 상품 후기 정보
	 * */
	public HashMap<String, Object> selectProductOne(HashMap<String, Object> param) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<String, Object> cmmnParam = new HashMap<String, Object>();
		
		result.put("productInfo", dao.selectProductOne(param)); // 상품 정보 조회
		
		return result;
	}
	
	/*
	 * 상품 후기 정보 조회
	 * */
	public HashMap<String, Object> selectProductReviewList(int curPage, int pageUnit, int blockUnit, HashMap<String, Object> param){
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<HashMap<String, Object>> reviewList = null;
		String reviewType = (String) param.get("review_type");
		
		int count = 0;
		
		if("PR002".equals(reviewType)) {
			count = dao.selectProductReviewImgListCount(param);
		} else {
			count = dao.selectProductReviewListCount(param);
		}
		
		Paging paging = new Paging(count, curPage, pageUnit, blockUnit);
		
		param.put("start", paging.getStart());
		param.put("end"	 , paging.getEnd());
		
		if("PR002".equals(reviewType)) {
			reviewList = dao.selectProductReviewImgList(param);
		} else {
			reviewList = dao.selectProductReviewList(param);
		}
		
		result.put("reviewList", reviewList);
		result.put("page", paging);
		
		result.put("reviewScope", this.selectProductReviewScope(param));	// 별점 조회
		
		return result;
	}
	
	/*
	 * 상품 리뷰 조회(단일 건)
	 * */
	public HashMap<String, Object> selectProductReviewDetail(HashMap<String, Object> param) 
			throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		result.put("reviewInfo", dao.selectProductReviewDetail(param));
		return result;
	}
	
	
	/*
	 * 상품 리뷰 별점 조회
	 * */
	public HashMap<String, Object> selectProductReviewScope(HashMap<String, Object> param){
		return dao.selectProductReviewScope(param);
	}
	
	/*
	 * 상품 리뷰 작성
	 * */
	@Transactional(rollbackFor = Exception.class)
	public HashMap<String, Object> insertProductReview(HashMap<String, Object> param
			, MultipartFile[] files) throws IOException, Exception {
		
		CmmnUser user = util.tokenToUserInfo();
		if(user != null) {
			param.put("id", user.getId());
		}
		
		String reviewType = (String) param.get("review_type");
		
		int review = dao.insertProductReview(param);
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		// 1. 상품 이미지 후기(PR001 : 일반 후기 / PR002 : 상품 이미지 후기 / PR003 : 익명 후기)
		if("PR002".equals(reviewType)) {
			if(files != null) {
				HashMap<String, Object> fileParam = new HashMap<String, Object>();
				
				String filePath = uploadPath + uploadFilePath;
				String reviewNo = (String) param.get("review_no");
				String[] ext	= {"image/png", "image/jpg", "image/jpeg", "image/gif"};
				int srtOdr 		= 1;
				
				fileParam.put("file_ref", reviewNo);
				fileParam.put("file_div", "PRODUCT_REVIEW");
				fileParam.put("id"		, user.getId());
				
				for(MultipartFile file: files) {
					if(!file.isEmpty()) {
						
						String fileName = util.uploadFile(file, filePath, ext);
						
						fileParam.put("file_src"	, uploadFilePath);
						fileParam.put("file_real_nm", fileName);
						fileParam.put("file_nm"		, file.getOriginalFilename());
						fileParam.put("srt_odr"		, srtOdr);

						// 상품 이미지 INSERT
						srtOdr += commonService.insertCmmnFile(fileParam);
					}
				}
			} else {
				logger.info("등록된 이미지가 없습니다.");
			}
		}
		
		
		if(review > 0) {
			result.put("code", "0000");
			result.put("message" , "리뷰 작성에 성공하였습니다.");
		} else {
			throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 상품 리뷰 삭제
	 * */
	public HashMap<String, Object> deleteProductReview(HashMap<String, Object> param) {
		int del = dao.deleteProductReview(param);
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		if(del > 0) {
			result.put("code", "0000");
			result.put("msg" , "상품 리뷰가 삭제되었습니다.");
		} else {
			result.put("code", "9999");
			result.put("msg" , "상품 리뷰 삭제에 실패하였습니다.");
		}
		return result;
	}
	
	/*
	 * 사용자 상품 장바구니 추가
	 * */
	public HashMap<String, Object> addProductToBasket(List<HashMap<String, Object>> listParam) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		String id = "";
		int insert = 0;
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			id = StringUtil.nullToBlank(user.getId());
		}
		
		for(HashMap<String, Object> param : listParam) {
			param.put("id", id);
			insert += dao.insertProductBasket(param);
		}
		
		result.put("result", insert);
		
		return result;
	}
	
	/*
	 * 사용자 상품 장바구니 제거
	 * */
	public HashMap<String, Object> deleteProductBasket(List<String> param) {
		
		if(param.size() < 1) throw new NullPointerException("제거할 상품이 존재하지 않습니다.");
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		int del = dao.deleteProductBasket(param);
		
		if(del > 0) {
			result.put("code", "0000");
			result.put("msg" , "장바구니에서 제거되었습니다.");
		} else {
			result.put("code", "9999");
			result.put("msg" , "장바구니 제거에 실패하였습니다.");
		}
		
		return result;
	}
	
	/*
	 * 사용자 상품 장바구니 수정 (갯수)
	 * */
	public HashMap<String, Object> updateProductBasket(@RequestBody HashMap<String, Object> param) {
		
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", user.getId());
		}
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		int upt = dao.updateProductBasket(param);
		
		if(upt > 0) {
			result.put("code", "0000");
			result.put("msg" , "변경사항이 저장되었습니다.");
		} else {
			result.put("code", "9999");
			result.put("msg" , "변경사항 저장에 실패하였습니다.");
		}
		return result;
	}
	
	/*
	 * 장바구니 조회
	 * */
	public HashMap<String, Object> selectProductBasket(){
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String, Object> result = new HashMap<String, Object>();
		CmmnUser user = util.tokenToUserInfo();

		if(user != null) {
			param.put("id", user.getId());
		}
		
		List<HashMap<String, Object>> basketList = dao.selectProductBasket(param);
		result.put("basketList", basketList);
		
		return result;
	}
	
	/*
	 * 상품 정보 입력
	 * */
	public int insertProductMst(HashMap<String, Object> param) {
		return dao.insertProductMst(param);
	}
	
	/*
	 * 상품 상세 정보 입력
	 * */
	public int insertProductList1(HashMap<String, Object> param) {
		return dao.insertProductList(param);
	}
	
	/*
	 * 상품 상세 정보 입력
	 * */
	public HashMap<String, Object> insertProductList(List<HashMap<String, Object>> params) 
			throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		int ins = 0;
		
		CmmnUser user = util.tokenToUserInfo();
		String id = "";
		
		if(user != null) {
			id = StringUtil.nullToBlank(user.getId());
		}
		
		for(HashMap<String, Object> param : params) {
			param.put("id", id);
			ins += dao.insertProductList(param);
		}
		
		if(ins > 0) {
			result.put("code", "0000");
			result.put("message", "상품이 추가되었습니다.");
		} else {
			throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	/*
	 * 상품 이미지 입력
	 * */
	public int insertProductFile(HashMap<String, Object> param) {
		return dao.insertProductFile(param);
	}
	
	/*
	 * 상품 정보 수정
	 * */
	public int updateProductMst(HashMap<String, Object> param) {
		return dao.updateProductMst(param);
	}
	
	/*
	 * 상품 상세 정보 수정
	 * */
	public int updateProductList1(HashMap<String, Object> param) {
		return dao.updateProductList(param);
	}
	
	/*
	 * 상품 상세 정보 수정
	 * */
	public HashMap<String, Object> updateProductList(HashMap<String, Object> param) 
			throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		int upt = 0;
		
		CmmnUser user = util.tokenToUserInfo();

		if(user != null) {
			param.put("id", user.getId());
		}
		
		upt += dao.updateProductList(param);
		
		if(upt > 0) {
			result.put("message", "상품이 변경되었습니다.");
		} else {
			throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 상품 이미지 수정
	 * */
	public int updateProductFile(HashMap<String, Object> param) {
		return dao.updateProductFile(param);
	}
	
	/*
	 * 상품 카테고리 리스트 조회
	 * */
	public HashMap<String, Object> getCategoryList(HashMap<String, Object> param){
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("categoryList", dao.selectCategoryList(param));
		return result;
	}
	
	/*
	 * 상품 재고 수량 체크
	 * param : product_no, product_cnt(구매 시, + / 환불 시, -)
	 * */
	public int updateProductListCnt(HashMap<String, Object> param) {
		return dao.updateProductListCnt(param);
	}
	
	/*
	 * 상품 판매량 통계 조회
	 * */
	public HashMap<String, Object> selectProductSalesCnt() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		result.put("categoryList", this.selectProductCategoryStsCnt());
		result.put("monthList"	, this.selectProductMonthStsCnt());
		result.put("sexList"	, this.selectProductSexStsCnt());
		
		return result;
	}
	
	/*
	 * 상품 카테고리별 판매량 조회
	 * */
	public List<HashMap<String, Object>> selectProductCategoryStsCnt(){
		return dao.selectProductCategoryStsCnt();
	}
	
	/*
	 * 월별 판매량 조회
	 * */
	public List<HashMap<String, Object>> selectProductMonthStsCnt(){
		return dao.selectProductMonthStsCnt();
	}
	
	/*
	 * 성별 판매량 조회
	 * */
	public List<HashMap<String, Object>> selectProductSexStsCnt(){
		return dao.selectProductSexStsCnt();
	}
}
