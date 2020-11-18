package org.loed.framework.common.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 与具体ORM实现无关的分页参数及查询结果封装. 注意所有序号从1开始.
 * 注:
 * 不需要json序列化的属性用@JsonIgnore标记
 *
 * @author Thomason
 */
@Data
public class Pagination<T> implements Serializable {
	/**
	 * 每页记录数
	 */
	private int pageSize = 10;
	/**
	 * 页码号
	 */
	private int pageNo = 1;
	/**
	 * 结果记录
	 */
	private List<T> rows;
	/**
	 * 总记录数
	 */
	private long total;

	public Pagination() {
	}

	public Pagination(int pageNo, int pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public <S> Pagination<S> map(Function<T, S> mapper) {
		Pagination<S> that = new Pagination<>();
		that.pageSize = this.pageSize;
		that.pageNo = this.pageNo;
		that.total = this.total;
		that.rows = this.rows == null ? null : this.rows.stream().map(mapper).collect(Collectors.toList());
		return that;
	}
}
