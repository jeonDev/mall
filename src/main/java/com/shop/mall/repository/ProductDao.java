package com.shop.mall.repository;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao {
	public int insertProductMst(HashMap<String, Object> product);		// 상품 등록
	public int insertProductList(HashMap<String, Object> param);		// 상품 세부내용 등록
	public int insertProductFile(HashMap<String, Object> product);		// 상품 이미지 등록
	
	public List<HashMap<String, Object>> selectProductList(HashMap<String, Object> param);		// 상품 리스트 조회
	public List<HashMap<String, Object>> selectProductSizeList(HashMap<String, Object> param);	// 상품 사이즈 리스트 조회
	public int selectProductListCount(HashMap<String, Object> param);							// 상품 리스트 조회 (건 수 조회)
	public HashMap<String, Object> selectProductOne(HashMap<String, Object> param);				// 상품 조회
	
	public int insertProductReview(HashMap<String, Object> param); // 상품 리뷰 작성
	public int deleteProductReview(HashMap<String, Object> param); // 상품 리뷰 삭제
	public List<HashMap<String, Object>> selectProductReviewList(HashMap<String, Object> param); 	// 상품 리뷰 조회(리스트)
	public HashMap<String, Object> selectProductReviewDetail(HashMap<String, Object> param); 		// 상품 리뷰 조회(단일건)
	public List<HashMap<String, Object>> selectProductReviewImgList(HashMap<String, Object> param); // 상품 리뷰 조회(이미지 추가)
	public int selectProductReviewListCount(HashMap<String, Object> param); 						// 상품 리뷰 조회 건수 조회
	public int selectProductReviewImgListCount(HashMap<String, Object> param); 						// 상품 리뷰 조회(이미지 추가) 건수 조회
	public HashMap<String, Object> selectProductReviewScope(HashMap<String, Object> param);			// 상품 리뷰 별점 조회
	
	public int updateProductMst(HashMap<String, Object> param); 	// 상품 정보 수정
	public int updateProductList(HashMap<String, Object> param); 	// 상품 상세 정보 수정
	public int updateProductListCnt(HashMap<String, Object> param); // 상품 구매 시, 재고 수량 체크
	public int updateProductFile(HashMap<String, Object> param); 	// 상품 파일 정보 수정
	
	public List<String> selectProductNoList(String product_id); // 상품 상세 번호 조회
	public List<HashMap<String, Object>> selectProductFileNoList(String product_id); // 상품 이미지 번호 조회
	
	public int insertProductBasket(HashMap<String, Object> param); // 사용자 상품 장바구니 추가
	public int deleteProductBasket(List<String> param); // 사용자 상품 장바구니 제거
	public int updateProductBasket(HashMap<String, Object> param); // 사용자 상품 장바구니 수정
	public List<HashMap<String, Object>> selectProductBasket(HashMap<String, Object> param); // 장바구니 조회
	
	public List<HashMap<String, Object>> selectCategoryList(HashMap<String, Object> param); //상품 카테고리 조회
	
	public List<HashMap<String, Object>> selectProductCategoryStsCnt();	// 카테고리별 상품 판매량
	public List<HashMap<String, Object>> selectProductMonthStsCnt();	// 월별 상품 판매량
	public List<HashMap<String, Object>> selectProductSexStsCnt();		// 성별 상품 판매량
	
	public HashMap<String, Object> selectProductStock(HashMap<String, Object> param);	// 상품 재고 체크
}
