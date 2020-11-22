package org.loed.framework.common.query;

import lombok.Data;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/10/31 16:44
 */
@Data
public class PageRequest {
	private int pageNumber = 1;
	private int pageSize = 10;
	private boolean paging;
	private boolean counting;

	public static PageRequest of(int pageNumber, int pageSize) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.setPageNumber(pageNumber);
		pageRequest.setPageSize(pageSize);
		return pageRequest;
	}

	public static PageRequest of(int pageNumber) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.setPageNumber(pageNumber);
		return pageRequest;
	}

	public long getOffset() {
		return (this.pageNumber - 1) * pageSize;
	}
}
