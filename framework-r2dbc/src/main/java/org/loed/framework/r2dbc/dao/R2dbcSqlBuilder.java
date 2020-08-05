package org.loed.framework.r2dbc.dao;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.query.Condition;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Operator;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/24 9:58 上午
 */
public interface R2dbcSqlBuilder {
	String BLANK = " ";
	String ROOT_TABLE_ALIAS_KEY = "t1";

	/**
	 * 构建Insert 语句
	 *
	 * @param entity 插入的对象
	 * @param table  表结构元数据
	 * @return Insert 语句及参数列表及参数类型
	 */
	R2dbcQuery insert(Object entity, Table table);

	/**
	 * 构造一个批量插入语句
	 *
	 * @param entityList 对象集合
	 * @param table      更细的对象信息
	 * @return Insert 语句及参数列表及参数类型
	 */
	R2dbcQuery batchInsert(List<?> entityList, Table table);

	/**
	 * 构造一个机遇r2dbc的更新语句
	 *
	 * @param entity     对象
	 * @param table      对象元信息
	 * @param conditions 动态更新条件
	 * @param includes   包含的更新的列，如果指定了，只更新包含的列，如果为空，将更新所有列
	 * @param excludes   排除更新的列，如果指定了，排除指定的列，如果为空，将不排除任何列
	 * @return 更新语句及参数
	 */
	R2dbcQuery updateByCondition(Object entity, Table table, List<Condition> conditions, Collection<String> includes, Collection<String> excludes);

	/**
	 * 按照动态条件构建一个删除对象的语句
	 * 如果对象中包含{@link org.loed.framework.common.po.IsDeleted} 列，则做逻辑删除
	 *
	 * @param table    表元信息
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 删除语句及参数
	 */
	<T> R2dbcQuery deleteByCriteria(Table table, Criteria<T> criteria);

	/**
	 * 按照动态条件构建一个查询语句
	 *
	 * @param table    表名
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery findByCriteria(Table table, Criteria<T> criteria);

	/**
	 * 按照动态条件构建一个查询语句
	 *
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery findPageByCriteria(Table table, Criteria<T> criteria, PageRequest pageRequest);

	/**
	 * 按照动态条件构建一个查询语句
	 *
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery countByCriteria(Criteria<T> criteria);

	/**
	 * 是否需要引号
	 *
	 * @return true 需要 false 不需要
	 */
	boolean isQuote();

	/**
	 * 获取引号的值
	 *
	 * @return 引号的值
	 */
	String quote();

	default String wrap(String raw) {
		if (isQuote()) {
			return quote() + raw + quote();
		}
		return raw;
	}

	/**
	 * 判断一个条件是否 合法的条件
	 *
	 * @param condition 条件
	 * @return true 合法，false 非法
	 */
	default boolean match(Condition condition) {
		Object value = condition.getValue();
		Operator operator = condition.getOperator();
		if (operator == Operator.isNull
				|| operator == Operator.isNotNull
				|| operator == Operator.blank
				|| operator == Operator.notBlank) {
			return true;
		}
		if (value == null) {
			return false;
		}
		if (value instanceof String && StringUtils.isBlank(String.valueOf(value))) {
			return false;
		}
		return true;
	}
}
