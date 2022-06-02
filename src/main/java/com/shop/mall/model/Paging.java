package com.shop.mall.model;

import lombok.Getter;

@Getter
// 페이징 처리
public class Paging {
	private int count;			// 전체 건 수
	private int curPage;		// 현재 페이지
	private int pageUnit;		// 한 페이지 출력 건 수
	private int start;			// 보여질 페이지 Start
	private int end;			// 보여질 페이지 End
	private int startBlock;		// Block Start
	private int endBlock;		// Block End
	private int blockUnit;		// 한 페이지 출력 페이지 수
	private int curBlock;		// 현재 Block
	private int blockCount;		// 전체 Block
	
	public Paging() {}
	
	public Paging(int count, int curPage, int pageUnit, int blockUnit) {
		this.count 		= count;
		this.curPage 	= curPage;
		this.pageUnit 	= pageUnit != 0 ? pageUnit : 10;
		this.blockUnit  = blockUnit != 0 ? blockUnit : 5;
		
		this.end 		= curPage * pageUnit;
		this.start		= (this.end - this.pageUnit) + 1;
		
		this.curBlock 	= (int) Math.ceil( (curPage != 1 ? curPage - 1 : 1) / blockUnit);
		int endCount 	= (int) Math.ceil( (count != 1 ? count - 1 : count) / pageUnit) + 1;
		
		this.startBlock = (this.curBlock * blockUnit) + 1;
		
		this.endBlock	= this.startBlock + (blockUnit - 1);
		this.endBlock	= this.endBlock > endCount ? endCount : this.endBlock;
		
		this.blockCount = (int) Math.ceil( (this.count / this.pageUnit) + (this.count % this.pageUnit == 0 ? 0 : 1) ); 
	}
	
}