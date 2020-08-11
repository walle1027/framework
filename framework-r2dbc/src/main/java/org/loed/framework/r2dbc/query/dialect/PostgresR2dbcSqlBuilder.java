package org.loed.framework.r2dbc.query.dialect;

import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.r2dbc.query.R2dbcQuery;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.function.Predicate;

/**
 * TODO
 *
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 10:34 上午
 */
public class PostgresR2dbcSqlBuilder implements R2dbcSqlBuilder {
	private final boolean quota;

	public PostgresR2dbcSqlBuilder(boolean quota) {
		this.quota = quota;
	}

	@Override
	public R2dbcQuery insert(Object entity, Table table) {
		return null;
	}

	@Override
	public R2dbcQuery batchInsert(List<?> entityList, Table table) {
		return null;
	}

	@Override
	public <T> R2dbcQuery updateByCriteria(Object entity, Table table, Criteria<T> criteria, Predicate<Column> columnFilter) {
		return null;
	}

	@Override
	public <T> R2dbcQuery deleteByCriteria(Table table, Criteria<T> criteria) {
		return null;
	}

	@Override
	public <T> R2dbcQuery findByCriteria(Table table, Criteria<T> criteria) {
		return null;
	}

	@Override
	public <T> R2dbcQuery findPageByCriteria(Table table, Criteria<T> criteria, PageRequest pageRequest) {
		return null;
	}

	@Override
	public <T> R2dbcQuery countByCriteria(Table table, Criteria<T> criteria) {
		return null;
	}

	@Override
	public boolean isQuote() {
		return quota;
	}

	@Override
	public String quote() {
		return "\"";
	}
}
