package com.shop.mall.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	NOT_FOUND(404, "COMMON-ERR-404", "페이지를 찾을 수 없습니다."),
	INTER_SERVER_ERROR(500, "COMMON-ERR-500", "서버 요청 시, 오류가 발생하였습니다."),
	SC_FORBIDDEN(403, "COMMON-ERR-403", "권한이 없는 사용자입니다.\n관리자에게 문의바랍니다."),
	SC_UNAUTHORIZED(401, "COMMON-ERR-401", "로그인이 필요한 기능입니다.")
	;
	
	public void setMessage(String message) {
		this.message = message;
	}
	private int status;
	private String errorCode;
	private String message;
}
