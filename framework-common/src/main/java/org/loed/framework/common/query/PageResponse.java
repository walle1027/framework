package org.loed.framework.common.query;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/6/7 上午12:06
 */
public class PageResponse<T> {
	/**
	 * 结果记录
	 */
	private List<T> rows;
	/**
	 * 总记录数
	 */
	private long totalCount;

	public <Y> PageResponse<Y> to(Function<T, Y> mapping) {
		PageResponse<Y> response = new PageResponse<>();
		response.setTotalCount(this.totalCount);
		response.setRows(rows == null ? null : rows.stream().map(mapping).collect(Collectors.toList()));
		return response;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
