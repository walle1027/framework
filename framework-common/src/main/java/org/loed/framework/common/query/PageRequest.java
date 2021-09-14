package org.loed.framework.common.query;

import lombok.Data;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/10/31 16:44
 */
@Data
public class PageRequest {
	private int pageNo = 1;
	private int pageSize = 10;
	private boolean paging = true;
	private boolean counting = true;

	public static PageRequest of(int pageNumber, int pageSize) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.setPageNo(pageNumber);
		pageRequest.setPageSize(pageSize);
		return pageRequest;
	}

	public static PageRequest of(int pageNumber) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.setPageNo(pageNumber);
		return pageRequest;
	}

	public long getOffset() {
		return (this.pageNo - 1) * pageSize;
	}

	public int getLimit() {
		return this.getPageSize();
	}
}
