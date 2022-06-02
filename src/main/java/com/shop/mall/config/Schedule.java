package com.shop.mall.config;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shop.mall.service.PaymentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Schedule {

	@Autowired
	private PaymentService payment;
	
	/*
	 * 결제 대기 주문 내역 제거!
	 * PAYMENT_STATE : PS / 하루 이상 지난 주문 제거
	 * 
	 * */
	// cron : 		     초 분 시간 일 월 요일
	@Scheduled(cron = "0 0 0 * * *")
	public void deleteUnPaymentOrder() {
		log.info("결제 대기 주문 내역 제거!");
		HashMap<String, Object> payInfo = payment.deleteUnPaymentOrder();
		log.info("=======================================");
		log.info("" + payInfo);
		log.info("=======================================");
	}
}
