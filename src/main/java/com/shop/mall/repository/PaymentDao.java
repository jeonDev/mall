package com.shop.mall.repository;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDao {
	public List<HashMap<String, Object>> getProductPayInfoBasketList(List<HashMap<String, Object>> param);	// 상품 결제정보 내역 조회 (To 장바구니)
	public List<HashMap<String, Object>> getProductPayInfoProductList(List<HashMap<String, Object>> param);	// 상품 결제정보 내역 조회 (To 구매하기)
	
	public int insertProductOrder(HashMap<String, Object> param); // 상품 주문내역 입력
	public int insertProductPayment(HashMap<String, Object> param); // 상품 구매내역 입력
	public int insertPaymentState(HashMap<String, Object> param); // 상품 구매내역 상태 입력
	
	public List<HashMap<String, Object>> selectProductOrderList(HashMap<String, Object> param); // 주문내역 조회
	public List<HashMap<String, Object>> selectPaymentStateList(HashMap<String, Object> param); // 주문상태 내역 조회
	
	public int updateProductPayment(HashMap<String, Object> param); // 상품 구매내역 수정
	public int deleteProductPayment(HashMap<String, Object> param); // 상품 구매내역 삭제
	public int deleteProductOrder(HashMap<String, Object> param); // 상품 주문내역 삭제
	
	public int deleteProductPaymentList(List<String> param);
	public int deleteProductOrderList(List<String> param);
	
	public HashMap<String, Object> selectPaymentPriceInfo(List<HashMap<String, Object>> param);
	public List<HashMap<String, Object>> selectOrderInfoList(HashMap<String, Object> param);		// 주문내역 조회
	public int selectOrderInfoListCnt(HashMap<String, Object> param);		// 주문내역 조회(수량)
	public List<HashMap<String, Object>> selectOrderPaymentStateCnt(HashMap<String, Object> param);	// 주문상태별 수량 조회
	
	public List<HashMap<String, Object>> selectUnPaymentOrder();	// 결제 안된 주문내역 조회
	
}
