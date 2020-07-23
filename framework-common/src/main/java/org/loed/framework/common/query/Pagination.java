package org.loed.framework.common.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

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
	 * 是否自动计算总行数
	 */
	private boolean needCount = true;
	/**
	 * 是否自动分页
	 */
	private boolean needPaging = true;
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
}
