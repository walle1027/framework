package org.loed.framework.r2dbc.query;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.query.Condition;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Operator;
import org.loed.framework.common.util.ReflectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import javax.persistence.GenerationType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

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
	 * 构造一个基于r2dbc的更新语句
	 *
	 * @param entity       对象
	 * @param table        对象元信息
	 * @param criteria     动态更新条件
	 * @param columnFilter 列过滤器
	 * @return 更新语句及参数
	 */
	<T> R2dbcQuery updateByCriteria(@NonNull Object entity, @NonNull Table table, @NonNull Criteria<T> criteria, @NonNull Predicate<Column> columnFilter);

	/**
	 * 按照动态条件构建一个删除对象的语句
	 * 如果对象中包含{@link org.loed.framework.common.po.IsDeleted} 列，则做逻辑删除
	 *
	 * @param table    表元信息
	 * @param criteria 动态条件
	 * @return 删除语句及参数
	 */
	<T> R2dbcQuery deleteByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件构建一个查询语句
	 *
	 * @param table    表名
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery findByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria);

	/**
	 * 按照动态条件构建一个分页查询语句
	 *
	 * @param table    查询对象
	 * @param criteria 动态条件
	 * @param pageable 分页参数
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery findPageByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria, @NonNull Pageable pageable);

	/**
	 * 按照动态条件构建一个查询语句
	 *
	 * @param table    查询对象信息
	 * @param criteria 动态条件
	 * @param <T>      对象类型
	 * @return 查询语句及参数
	 */
	<T> R2dbcQuery countByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria);

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

	/**
	 * 从查询条件中获取对象的真实的表名，如果是分库分表的情况下
	 *
	 * @param table    ORM映射对象
	 * @param criteria 查询条件
	 * @param <T>      对象泛型
	 * @return 真实的表名
	 */
	default <T> String getTableNameByCriteria(Table table, Criteria<T> criteria) {
		return table.getSqlName();
//		if (!table.isSharding()) {
//			return table.getSqlName();
//		}
//		if (criteria == null || CollectionUtils.isEmpty(criteria.getConditions())) {
//			throw new RuntimeException("empty conditions");
//		}
//		//优先检查condition中是否包含分表的值
//		List<Column> shardingColumns = table.getColumns().stream().filter(Column::isShardingColumn).collect(Collectors.toList());
//		List<String> shardingValues = new ArrayList<>();
//		shardingColumns.forEach(r -> {
//			criteria.getConditions().stream().filter(k -> k.getPropertyName().equals(r.getJavaName()) && k.getOperator().equals(Operator.equal)).findFirst().ifPresent(condition -> shardingValues.add(String.valueOf(condition.getValue())));
//		});
//		if (shardingValues.size() != shardingColumns.size()) {
//			String idValue = table.getColumns().stream().filter(Column::isPk).sorted(Comparator.comparing(Column::getJavaName)).map(column -> {
//				Object value = criteria.getConditions().stream().filter(k -> column.getJavaName().equals(k.getPropertyName()) && k.getOperator().equals(Operator.equal)).findFirst().map(Condition::getValue).orElse(null);
//				return String.valueOf(value);
//			}).collect(Collectors.joining(","));
//			return getShardingManager().getShardingTableNameById(table, idValue);
//		} else {
//			String shardingValue = getShardingValue(shardingValues);
//			return getShardingManager().getShardingTableNameByValue(table, shardingValue);
//		}
	}

	default String createTableAlias(String tableName, AtomicInteger counter) {
		return "t" + counter.getAndIncrement();
	}

	/**
	 * 默认过滤器
	 */
	Predicate<Column> ALWAYS_TRUE_FILTER = column -> true;

	/**
	 * 可插入列的过滤器
	 */
	Predicate<Column> INSERTABLE_FILTER = column -> {
		boolean pk = column.isPk();
		if (pk) {
			GenerationType generationType = column.getIdGenerationType();
			if (Objects.equals(generationType, GenerationType.AUTO)) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	};

	/**
	 * 可更新列的过滤器
	 */
	Predicate<Column> UPDATABLE_FILTER = Column::isUpdatable;
	/**
	 * 版本列的过滤器
	 */
	Predicate<Column> VERSION_FILTER = Column::isVersioned;

	/**
	 * 指定更新列的过滤器
	 */
	class IncludeFilter implements Predicate<Column> {
		private final Collection<String> includes;

		public IncludeFilter(@NonNull Collection<String> includes) {
			this.includes = includes;
		}

		@Override
		public boolean test(Column column) {
			return includes.contains(column.getJavaName());
		}
	}

	/**
	 * 指定忽略列的过滤器
	 */
	class ExcludeFilter implements Predicate<Column> {
		private final Collection<String> excludes;

		public ExcludeFilter(@NonNull Collection<String> excludes) {
			this.excludes = excludes;
		}

		@Override
		public boolean test(Column column) {
			return !excludes.contains(column.getJavaName());
		}
	}

	/**
	 * 动态判断属性为空的过滤器
	 */
	class NonBlankFilter implements Predicate<Column> {
		private final Object object;

		public NonBlankFilter(@NonNull Object object) {
			this.object = object;
		}

		@Override
		public boolean test(Column column) {
			if (column.isPk()) {
				return false;
			}
			if (column.isVersioned()) {
				return false;
			}
			Object value = ReflectionUtils.getFieldValue(object, column.getJavaName());
			if (value == null) {
				return false;
			}
			if (value instanceof String) {
				return StringUtils.isNotBlank((String) value);
			}
			return true;
		}
	}
}
