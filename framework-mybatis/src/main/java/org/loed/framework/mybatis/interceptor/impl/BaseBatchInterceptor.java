package org.loed.framework.mybatis.interceptor.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Invocation;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.ORMapping;
import org.loed.framework.common.orm.Table;
import org.loed.framework.mybatis.BatchOperation;
import org.loed.framework.mybatis.BatchType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;


public abstract class BaseBatchInterceptor extends BasePreProcessInterceptor<Triple<BatchType, List<Object>, Table>> implements BatchOperation {


	protected int batchSize = 500;

	public BaseBatchInterceptor() {
	}

	protected BaseBatchInterceptor(int batchSize) {
		this.batchSize = batchSize;
	}

	@Override
	protected Triple<BatchType, List<Object>, Table> preProcess(Invocation invocation) {
		BatchType batchType = getBatchType(invocation);
		if (batchType == BatchType.None) {
			return Triple.of(batchType, null, null);
		}
		Object[] args = invocation.getArgs();
		Object parameterMap = args[1];
		if (!(parameterMap instanceof HashMap)) {
			throw new RuntimeException("mybatis parameterMap is not a hash map");
		}
		HashMap<Object, Object> parameterHashMap = (HashMap<Object, Object>) parameterMap;
		Object parameter = parameterHashMap.get("list");
		if (!(parameter instanceof List)) {
			throw new RuntimeException("parameter of batch insert is not a list");
		}
		List<Object> poList = (List<Object>) parameter;
		if (CollectionUtils.isEmpty(poList)) {
			return Triple.of(batchType, poList, null);
		}
		Object firstPo = poList.get(0);
		Table table = ORMapping.get(firstPo.getClass());
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + firstPo.getClass().getName());
		}
		return Triple.of(batchType, poList, table);
	}


	@Override
	protected Object doIntercept(Invocation invocation, Triple<BatchType, List<Object>, Table> context) throws Throwable {
		Object[] args = invocation.getArgs();
		Object parameterMap = args[1];
		Executor executor = (Executor) invocation.getTarget();
		return doBatch(executor, context, (HashMap<Object, Object>) parameterMap);
	}


	private int doBatch(Executor executor, Triple<BatchType, List<Object>, Table> context, HashMap<Object, Object> parameterMap) throws SQLException {
		BatchType batchType = context.getLeft();
		List<Object> poList = context.getMiddle();
		if (CollectionUtils.isEmpty(poList)) {
			return 0;
		}
		Table table = context.getRight();

		if (CollectionUtils.isEmpty(table.getColumns())) {
			throw new RuntimeException("table " + table.getJavaName() + " doesn't have allColumns");
		}

		int rows = 0;
		try {
			switch (batchType) {
				case BatchInsert:
					rows = doBatchInsert(executor, poList, table);
					break;
				case BatchUpdateNonBlank:
					rows = doBatchUpdateNonBlank(executor, poList, table, (Predicate<Column>) parameterMap.get("predicate"));
					break;
				case BatchUpdateNonNull:
					rows = doBatchUpdateNonNull(executor, poList, table, (Predicate<Column>) parameterMap.get("predicate"));
					break;
				case BatchUpdateFixed:
					rows = doBatchUpdate(executor, poList, table, (Predicate<Column>) parameterMap.get("predicate"));
					break;
				default:
					break;
			}
		} finally {
			executor.clearLocalCache();
		}
		return rows;
	}

	/**
	 * 批量新增
	 *
	 * @param executor
	 * @param poList
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	protected abstract int doBatchInsert(Executor executor, List<Object> poList, Table table) throws SQLException;

	/**
	 * 批量更新
	 *
	 * @param executor
	 * @param poList
	 * @param table
	 * @param predicate
	 * @return
	 * @throws SQLException
	 */
	protected abstract int doBatchUpdateNonBlank(Executor executor, List<Object> poList, Table table, Predicate<Column> predicate) throws SQLException;

	/**
	 * 批量更新
	 *
	 * @param executor
	 * @param poList
	 * @param table
	 * @param predicate
	 * @return
	 * @throws SQLException
	 */
	protected abstract int doBatchUpdateNonNull(Executor executor, List<Object> poList, Table table, Predicate<Column> predicate) throws SQLException;

	/**
	 * 批量更新
	 *
	 * @param executor
	 * @param poList
	 * @param table
	 * @param predicate
	 * @return
	 * @throws SQLException
	 */
	protected abstract int doBatchUpdate(Executor executor, List<Object> poList, Table table, Predicate<Column> predicate) throws SQLException;
}
