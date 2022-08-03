package com.shop.mall.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.mall.config.DuplicationException;
import com.shop.mall.config.ErrorCode;
import com.shop.mall.model.CmmnUser;
import com.shop.mall.model.Paging;
import com.shop.mall.repository.PaymentDao;
import com.shop.mall.util.StringUtil;
import com.shop.mall.util.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	@Autowired
	private PaymentDao dao;
	
	@Autowired
	private ProductService productService;
	
	private final Utils util;
	
	/*
	 * 결제 내역 조회
	 * */
	public HashMap<String, Object> getProductPayInfoList(HashMap<String, Object> param, String payInfo) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<HashMap<String, Object>> list = null;
		List<HashMap<String, Object>> listParam = new ArrayList<HashMap<String,Object>>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		System.out.println(payInfo);
		param.forEach((key, value)->{
			HashMap<String, Object> map = new HashMap<String, Object>();

			String jsonStr = StringUtil.nullToBlank((String) value);
			
			/* String -> Map */
			try {
				map = mapper.readValue(jsonStr, 
						new HashMap<String, Object>().getClass());
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			listParam.add(map);
		});
		
		if("basket".equals(payInfo)) {
			list = dao.getProductPayInfoBasketList(listParam);
		} else if("product".equals(payInfo)) {
			list = dao.getProductPayInfoProductList(listParam);
		}
		
		result.put("productPayList", list);
		
		return result;
	}
	
	/*
	 * 상품 결제 처리
	 * */
	@Transactional(rollbackFor = Exception.class)
	public HashMap<String, Object> productPayment(HashMap<String, Object> param) 
			throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<HashMap<String, Object>> listParam = new ArrayList<HashMap<String,Object>>();
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		listParam = (List) param.get("productPayList");

		// 상품 가격 및 이름 조회
		HashMap<String, Object> payInfo = dao.selectPaymentPriceInfo(listParam);
		result.put("payInfo", payInfo);
		
		// 주문내역 테이블 INSERT
		HashMap<String, Object> orderParam = (HashMap<String, Object>) param.get("addrInfo");
		orderParam.put("id", id);
		orderParam.put("pay_method", "card");
		orderParam.put("merchant_uid", "merchant_" + new Date().getTime());
		orderParam.put("product_name", payInfo.get("PRODUCT_NAME"));
		orderParam.put("amount", payInfo.get("AMOUNT"));
		
		dao.insertProductOrder(orderParam);

		// 결제내역 테이블 INSERT
		int payPrice1 = ((BigDecimal) payInfo.get("AMOUNT")).intValue();
		int payPrice2 = 0;
		for(HashMap<String, Object> map:listParam) {
			map.put("order_no", orderParam.get("order_no"));
			map.put("payment_way", "card");
			map.put("payment_yn", "N");
			map.put("payment_state", "PS");
			map.put("id", id);
			
			// 구매내역 입력
			dao.insertProductPayment(map);
			
			HashMap<String, Object> item = (HashMap<String, Object>) map.get("item");
			
			// 주문상태 내역 기록
			String paymentNo = (String) item.get("PAYMENT_NO");
			
			HashMap<String, Object> stateParam = (HashMap<String, Object>) map.get("item");
			
			stateParam.put("id", id);
			stateParam.put("payment_state", map.get("payment_state"));
			stateParam.put("payment_no", paymentNo);
			
			this.insertPaymentState(stateParam);
			
			// 상품 가격 더한것과 총 금액이 같은지 체크. (Exception 관리자에게 문의!)
			int buyPrice = ((BigDecimal) item.get("BUY_PRICE")).intValue(); // Oracle
//			int buyPrice = ((Long) item.get("BUY_PRICE")).intValue();
			
			payPrice2 += buyPrice;
		}
		
		// 결제금액 체크
		if(payPrice1 == payPrice2) {
			result.put("orderInfo", orderParam);
		} else {
			throw new DuplicationException("결제 시 오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 상품 결제 콜백
	 * */
	public HashMap<String, Object> productPaymentCallback(HashMap<String, Object> param) 
			throws SQLIntegrityConstraintViolationException {
		HashMap<String, Object> result = new HashMap<String, Object>();

		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		String paymentYn = (String) param.get("paymentYn");
		
		if("Y".equals(paymentYn)) {
			param.put("payment_state", "PC");	// 결제 완료
			param.put("payment_yn", "Y");		// 결제 완료
			dao.updateProductPayment(param);	// 결제 완료 처리
			
			if(param.containsKey("basket_no")) {
				List<String> basketNo = (List<String>) param.get("basket_no");
				
				productService.deleteProductBasket(basketNo);	// 장바구니 제거
			}
			
			List<HashMap<String, Object>> buyCntParam = (List<HashMap<String, Object>>) param.get("buyInfo");
			
			for(HashMap<String, Object> buyCnt : buyCntParam) {
				buyCnt.put("id", id);
				productService.updateProductListCnt(buyCnt);		// 상품 갯수 - 처리
			}
			
		} else {
			this.deleteProductPayment(param);	// 결제내역 제거
			this.deleteProductOrder(param);		// 주문내역 제거
		}
		
		
		
		return result;
	}
	/*
	 * 사용자 주문내역 조회
	 * */
	public List<HashMap<String, Object>> productOrderList(){
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		String id = user.getId();
		
		if(user != null) {
			param.put("id", id);
		}
		
		List<HashMap<String, Object>> result = dao.selectProductOrderList(param);
		
		return result;
	}
	
	/*
	 * 주문번호 조회
	 * */
	public List<HashMap<String, Object>> productOrderByNoList(String orderNo){
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		param.put("order_no", orderNo);
		
		List<HashMap<String, Object>> result = dao.selectProductOrderList(param);
		return result;
	}
	
	/*
	 * 주문 상태 내역 조회
	 * */
	public List<HashMap<String, Object>> selectPaymentStateList(String paymentNo){
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		param.put("payment_no", paymentNo);
		
		return dao.selectPaymentStateList(param);
	}
	
	/*
	 * 주문 내역 상태 변경
	 * */
	public HashMap<String, Object> updatePaymentState(HashMap<String, Object> param) 
			throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int upt = 0;
		int ins = 0;
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", (String) user.getId());
		}
		
		upt += dao.updateProductPayment(param);
		
		if(upt > 0) {
			ins += this.insertPaymentState(param);
		}
		
		if(ins > 0) {
			result.put("msg", "주문 상태가 변경되었습니다.");
		} else {
			throw new DuplicationException("오류가 발생하였습니다.\n관리자에게 문의바랍니다.", ErrorCode.INTER_SERVER_ERROR);
		}
		
		return result;
	}
	
	/*
	 * 주문내역 조회
	 * */
	public HashMap<String, Object> selectOrderInfoList(int curPage, int pageUnit, int blockUnit, 
			HashMap<String, Object> param) throws Exception {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", (String) user.getId());
			List<String> roles = user.getRoles();
			boolean roleAdminChk = roles.contains("ROLE_ADMIN");
			param.put("role", roleAdminChk);
		}
		
		int count = dao.selectOrderInfoListCnt(param);
		Paging paging = new Paging(count, curPage, pageUnit, blockUnit);
		
		param.put("start", paging.getStart());
		param.put("end"	 , paging.getEnd());
		List<HashMap<String, Object>> orderList = dao.selectOrderInfoList(param);
		
		result.put("orderInfo", orderList);
		result.put("page", paging);
		
		return result;
	}
	
	/*
	 * 주문상태별 수량 조회
	 * */
	public HashMap<String, Object> selectOrderPaymentStateCnt() 
			throws Exception {
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		// User Id Get
		CmmnUser user = util.tokenToUserInfo();
		
		if(user != null) {
			param.put("id", (String) user.getId());
		}
		
		result.put("paymentStateCnt", dao.selectOrderPaymentStateCnt(param));
		
		return result;
	}
	
	public int excel(HttpServletResponse response, 
			HashMap<String, Object> excel) throws IOException{
		return util.excelDownload(response, excel);
	}
	
	
	/*
	 * 결제내역 삭제
	 * */
	public void deleteProductPayment(HashMap<String, Object> param){
		dao.deleteProductPayment(param);
	}
	
	/*
	 * 주문내역 삭제
	 * */
	public void deleteProductOrder(HashMap<String, Object> param){
		dao.deleteProductOrder(param);
	}
	
	/*
	 * 결제내역 삭제
	 * */
	public void deleteProductPaymentList(List<String> param){
		dao.deleteProductPaymentList(param);
	}
	
	/*
	 * 주문내역 삭제
	 * */
	public void deleteProductOrderList(List<String> param){
		dao.deleteProductOrderList(param);
	}
	
	/*
	 * 주문상태 내역 입력(내역 기록)
	 * */
	public int insertPaymentState(HashMap<String, Object> param) {
		return dao.insertPaymentState(param);
	}
	
	/*
	 * 결제 대기 주문 내역 제거!
	 * PAYMENT_STATE : PS / 하루 이상 지난 주문 제거
	 * */
	public HashMap<String, Object> deleteUnPaymentOrder() {
		HashMap<String, Object> param = new HashMap<String, Object>();
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		List<HashMap<String, Object>> listPaymentOrder = dao.selectUnPaymentOrder();
		List<String> listOrderNo = new ArrayList<String>();
		if(!listPaymentOrder.isEmpty()) {
			for(HashMap<String, Object> list : listPaymentOrder) {
				HashMap<String, Object> map = list;
				System.out.println(map);
				String orderNo = (String) list.get("ORDER_NO");
				
				listOrderNo.add(orderNo);
			}
			
			this.deleteProductPaymentList(listOrderNo);
			this.deleteProductOrderList(listOrderNo);
			
			result.put("paymentInfo", listPaymentOrder);
		} else {
			result.put("paymentInfo", "제거된 주문내역 없음");
		}
		return result;
	}
}
