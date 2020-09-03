package org.loed.framework.r2dbc.query.dialect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Join;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.query.*;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.query.R2dbcQuery;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import javax.persistence.GenerationType;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/24 9:59 上午
 */
@Slf4j
public class MysqlR2dbcSqlBuilder implements R2dbcSqlBuilder {

	private final boolean quote;

	public MysqlR2dbcSqlBuilder(boolean quote) {
		this.quote = quote;
	}

	@Override
	public R2dbcQuery insert(Object entity, Table table) {
		R2dbcQuery query = new R2dbcQuery();
		StringBuilder builder = new StringBuilder();
		Map<String, R2dbcParam> params = new HashMap<>();
		builder.append("insert into ").append(wrap(table.getSqlName())).append("(");
		List<Column> columns = table.getColumns().stream().filter(INSERTABLE_FILTER).collect(Collectors.toList());
		columns.forEach(column -> {
			builder.append(wrap(column.getSqlName()));
			builder.append(",");
			params.put(column.getJavaName(), new R2dbcParam(column.getJavaType(), ReflectionUtils.getFieldValue(entity, column.getJavaName())));
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(") values ( ");
		columns.forEach(column -> {
			builder.append(":").append(column.getJavaName()).append(",");
		});
		builder.deleteCharAt(builder.length() - 1).append(")");
		if (table.getIdGenerationType() == GenerationType.AUTO) {
			builder.append("; select last_insert_id()");
		}
		query.setParams(params);
		query.setStatement(builder.toString());
		return query;
	}


	@Override
	public R2dbcQuery batchInsert(List<?> entityList, Table table) {
		R2dbcQuery query = new R2dbcQuery();
		StringBuilder builder = new StringBuilder();
		Map<String, R2dbcParam> params = new HashMap<>();
		builder.append("insert into ").append(wrap(table.getSqlName())).append("(");
		List<Column> columns = table.getColumns().stream().filter(INSERTABLE_FILTER).collect(Collectors.toList());
		columns.forEach(column -> {
			builder.append(wrap(column.getSqlName()));
			builder.append(",");
		});
		builder.deleteCharAt(builder.length() - 1);
		builder.append(") values ");
		for (int i = 0; i < entityList.size(); i++) {
			Object object = entityList.get(i);
			builder.append("(");
			int finalI = i;
			columns.forEach(column -> {
				builder.append(":").append(column.getJavaName()).append(finalI).append(",");
				params.put(column.getJavaName() + finalI, new R2dbcParam(column.getJavaType(), ReflectionUtils.getFieldValue(object, column.getJavaName())));
			});
			builder.deleteCharAt(builder.length() - 1).append(")").append(",");
		}
		builder.deleteCharAt(builder.length() - 1);
		if (table.getIdGenerationType() == GenerationType.AUTO) {
			builder.append("; select last_insert_id(),row_count()");
		}
		query.setStatement(builder.toString());
		query.setParams(params);
		return query;
	}

	@Override
	public <T> R2dbcQuery updateByCriteria(@NonNull Object entity, @NonNull Table table, @NonNull Criteria<T> criteria, @NonNull Predicate<Column> columnFilter) {
		R2dbcQuery query = new R2dbcQuery();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> params = new HashMap<>();
		QueryBuilder builder = new QueryBuilder();
		builder.update(wrap(table.getSqlName()));
		table.getColumns().parallelStream().filter(UPDATABLE_FILTER.and(columnFilter).or(VERSION_FILTER)).forEach(column -> {
			StringBuilder setBuilder = new StringBuilder();
			if (column.isVersioned()) {
				setBuilder.append(wrap(column.getSqlName())).append(BLANK).append("=").append(BLANK).append(wrap(column.getSqlName())).append(" + 1");
			} else {
				setBuilder.append(wrap(column.getSqlName())).append(BLANK).append("=").append(BLANK).append(":").append(column.getJavaName());
				Object fieldValue = ReflectionUtils.getFieldValue(entity, column.getJavaName());
				params.put(column.getJavaName(), new R2dbcParam(column.getJavaType(), fieldValue));
			}
			builder.set(setBuilder.toString());
		});
		if (CollectionUtils.isEmpty(builder.getUpdateList())) {
			throw new RuntimeException("empty columns to update");
		}
		List<Condition> conditions = criteria.getConditions();
		PropertySelector selector = criteria.getSelector();
		if (CollectionUtils.isEmpty(conditions)) {
			log.warn("conditions is empty, the statement:" + builder.toString() + " will update all rows");
		} else {
			buildConditions(table, builder, counter, params, conditions, selector, Collections.emptyMap());
		}
		query.setStatement(builder.toString());
		query.setParams(params);
		return query;
	}

	@Override
	public <T> R2dbcQuery deleteByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria) {
		R2dbcQuery query = new R2dbcQuery();
		QueryBuilder builder = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> paramMap = new HashMap<>();

		Column isDeletedColumn = table.getColumns().stream().filter(Column::isDeleted).findFirst().orElse(null);
		if (isDeletedColumn != null) {
			builder.update(wrap(table.getSqlName()));
			builder.set(wrap(isDeletedColumn.getSqlName()) + BLANK + "=" + BLANK + "1");
		} else {
			builder.delete(wrap(table.getSqlName()));
		}
		List<Condition> conditions = criteria.getConditions();
		PropertySelector selector = criteria.getSelector();
		if (CollectionUtils.isEmpty(conditions)) {
			log.warn("criteria is empty condition");
		} else {
			buildConditions(table, builder, counter, paramMap, conditions, selector, Collections.emptyMap());
		}
		query.setStatement(builder.toString());
		query.setParams(paramMap);
		return query;
	}

	private void buildConditions(@NonNull Table table, QueryBuilder builder, AtomicInteger counter, Map<String, R2dbcParam> paramMap, List<Condition> conditions
			, PropertySelector selector, Map<String, String> tableAliasMap) {
		conditions.sort((o1, o2) -> {
			if (o1.getPropertyName() == null) {
				return -1;
			}
			if (o2.getPropertyName() == null) {
				return 1;
			}
			return o1.getPropertyName().compareTo(o2.getPropertyName());
		});
		for (Condition condition : conditions) {
			buildCondition(paramMap, tableAliasMap, counter, builder, condition, table, selector);
		}
	}

	@Override
	public <T> R2dbcQuery findByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria) {
		R2dbcQuery query = new R2dbcQuery();
		QueryBuilder builder = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> paramMap = new HashMap<>();
		String tableName = table.getSqlName();
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(tableName, counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, rootAlias);
		PropertySelector selector = criteria.getSelector();
		table.getColumns().stream().filter(column -> {
			if (selector != null) {
				if (selector.getIncludes() != null) {
					return selector.getIncludes().contains(column.getJavaName());
				} else if (selector.getExcludes() != null) {
					return !selector.getExcludes().contains(column.getJavaName());
				}
			}
			return true;
		}).forEach(column -> {
			builder.select(rootAlias + "." + wrap(column.getSqlName()) + " as " + "\"" + column.getJavaName() + "\"");
		});
		builder.from(wrap(tableName) + " as " + rootAlias);
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			buildConditions(table, builder, counter, paramMap, conditions, selector, tableAliasMap);
		}
		buildOrder(tableAliasMap, counter, builder, table, criteria.getSortProperties(), selector);
		if (log.isDebugEnabled()) {
			log.debug(builder.toString());
		}
		query.setStatement(builder.toString());
		query.setParams(paramMap);
		return query;
	}

	@Override
	public <T> R2dbcQuery findPageByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria, @NonNull Pageable pageable) {
		R2dbcQuery query = findByCriteria(table, criteria);
		String statement = query.getStatement();
		Map<String, R2dbcParam> params = query.getParams();
		if (params == null) {
			params = new HashMap<>();
		}
		String prstmt;
		if (pageable.getPageNumber() > 1) {
			prstmt = statement + BLANK + "limit :pr_limit offset :pr_offset";
			params.put("pr_limit", new R2dbcParam(Integer.class, pageable.getPageSize()));
			params.put("pr_offset", new R2dbcParam(Long.class, pageable.getOffset()));
		} else {
			prstmt = statement + BLANK + "limit :pr_limit";
			params.put("pr_limit", new R2dbcParam(Integer.class, pageable.getPageSize()));
		}
		query.setStatement(prstmt);
		query.setParams(params);
		return query;
	}

	@Override
	public <T> R2dbcQuery countByCriteria(@NonNull Table table, @NonNull Criteria<T> criteria) {
		R2dbcQuery query = new R2dbcQuery();
		QueryBuilder builder = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> paramMap = new HashMap<>();
		String tableName = table.getSqlName();
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(tableName, counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, rootAlias);
		builder.select("count(1)").from(wrap(tableName) + " as " + rootAlias);
		List<Condition> conditions = criteria.getConditions();
		PropertySelector selector = criteria.getSelector();
		if (CollectionUtils.isNotEmpty(conditions)) {
			buildConditions(table, builder, counter, paramMap, conditions, selector, tableAliasMap);
		}
		// do not include order by clause
		if (log.isDebugEnabled()) {
			log.debug(builder.toString());
		}
		query.setStatement(builder.toString());
		query.setParams(paramMap);
		return query;
	}

	@Override
	public boolean isQuote() {
		return this.quote;
	}

	@Override
	public String quote() {
		return "`";
	}

	public void buildCondition(Map<String, R2dbcParam> parameterMap, Map<String, String> tableAliasMap, AtomicInteger counter
			, QueryBuilder sql, Condition condition, Table table, PropertySelector selector) {
		if (condition.hasSubCondition()) {
			if (condition.getJoint() != null) {
				sql.where(condition.getJoint().name() + BLANK + "(");
			} else {
				sql.where("(");
			}
			for (Condition subCondition : condition.getSubConditions()) {
				buildCondition(parameterMap, tableAliasMap, counter, sql, subCondition, table, selector);
			}
			sql.where(")");
		} else {
			buildSingleCondition(parameterMap, tableAliasMap, counter, sql, condition, table, selector);
		}
	}

	private void buildSingleCondition(Map<String, R2dbcParam> parameterMap, Map<String, String> tableAliasMap, AtomicInteger counter
			, QueryBuilder sql, Condition condition, Table table, PropertySelector selector) {
		if (!match(condition)) {
			log.warn("condition :" + condition + " is not a valid condition,will ignore");
			return;
		}
		String propertyName = condition.getPropertyName();
		String joint = (condition.getJoint() == null ? "" : condition.getJoint().name()) + BLANK;
		Object value = condition.getValue();
		String rawParamName = StringUtils.replace(propertyName, ".", "_") + "Value";
		String uniqueParamName = genUniqueParamName(rawParamName, parameterMap);
		String alias = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY);
		Column column = resolvePropertyCascade(tableAliasMap, table, alias, counter, sql, condition.getJoinType(), null, propertyName, selector);
		if (column == null) {
			throw new RuntimeException("can't find  column from condition:" + condition);
		}
		String columnName = column.getSqlName();
		Class<?> columnType = column.getJavaType();
		int dataType = DataType.getDataType(columnType);
		if (condition.isRelativeProperty()) {
			alias = tableAliasMap.get(propertyName.substring(0, propertyName.lastIndexOf(".")));
		}
		String columnNameAlias;
		if (alias == null) {
			columnNameAlias = wrap(columnName);
		} else {
			columnNameAlias = alias + "." + wrap(columnName);
		}

		switch (condition.getOperator()) {
			case beginWith:
			case notBeginWith:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + ":" + uniqueParamName);
				parameterMap.put(uniqueParamName, new R2dbcParam(columnType, value + "%"));
				break;
			case endWith:
			case notEndWith:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + ":" + uniqueParamName);
				parameterMap.put(uniqueParamName, new R2dbcParam(columnType, "%" + value));
				break;
			case contains:
			case notContains:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + ":" + uniqueParamName);
				parameterMap.put(uniqueParamName, new R2dbcParam(columnType, "%" + value + "%"));
				break;
			case between:
			case notBetween:
				String start = genUniqueParamName(uniqueParamName + "Start", parameterMap);
				String end = genUniqueParamName(uniqueParamName + "End", parameterMap);
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + ":" + start + BLANK
						+ "and" + BLANK + ":" + end);
				if (value instanceof Collection) {
					if (((Collection) value).size() < 2) {
						throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
					}
					int i = 0;
					for (Object v : (Collection<?>) value) {
						if (i == 0) {
							parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.getDataType(v.getClass()), dataType)));
						}
						if (i == 1) {
							parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.getDataType(v.getClass()), dataType)));
						}
						i++;
					}
				} else if (value instanceof String) {
					String[] betweenValues = StringUtils.split((String) value, ",");
					if (betweenValues.length < 2) {
						throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
					}
					parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(betweenValues[0], DataType.DT_String, dataType)));
					parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(betweenValues[1], DataType.DT_String, dataType)));
				} else if (value.getClass().isArray()) {
					String simpleName = value.getClass().getSimpleName();
					switch (simpleName) {
						case "int[]": {
							int[] values = (int[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								int v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_int, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_int, dataType)));
								}
							}
							break;
						}
						case "long[]": {
							long[] values = (long[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								long v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_long, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_long, dataType)));
								}
							}
							break;
						}
						case "char[]": {
							char[] values = (char[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								char v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_char, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_char, dataType)));
								}
							}
							break;
						}
						case "double[]": {
							double[] values = (double[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								double v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_double, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_double, dataType)));
								}
							}
							break;
						}
						case "byte[]": {
							byte[] values = (byte[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								byte v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_byte, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_byte, dataType)));
								}
							}
							break;
						}
						case "short[]": {
							short[] values = (short[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								short v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_short, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_short, dataType)));
								}
							}
							break;
						}
						case "boolean[]": {
							boolean[] values = (boolean[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								boolean v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_boolean, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_boolean, dataType)));
								}
							}
							break;
						}
						case "float[]": {
							float[] values = (float[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								float v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_float, dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.DT_float, dataType)));
								}
							}
							break;
						}
						default: {
							Object[] values = (Object[]) value;
							if (values.length < 2) {
								throw new IllegalArgumentException("parameter " + value + " length less than 2 when use between operator");
							}
							for (int i = 0; i < values.length; i++) {
								Object v = values[i];
								if (i == 0) {
									parameterMap.put(start, new R2dbcParam(columnType, DataType.toType(v, DataType.getDataType(v.getClass()), dataType)));
								}
								if (i == 1) {
									parameterMap.put(end, new R2dbcParam(columnType, DataType.toType(v, DataType.getDataType(v.getClass()), dataType)));
								}
							}
							break;
						}
					}
				}
				break;
			case blank:
			case notBlank:
			case isNull:
			case isNotNull:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value());
				break;
			case equal:
			case notEqual:
			case lessEqual:
			case lessThan:
			case greaterEqual:
			case greaterThan:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + ":" + uniqueParamName);
				parameterMap.put(uniqueParamName, new R2dbcParam(columnType, DataType.toType(value, dataType)));
				break;
			case in:
			case notIn:
				if (value instanceof Collection) {
					Collection<?> collectionValue = (Collection<?>) value;
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					if (columnType.getName().equals(String.class.getName())) {
						for (Object inValue : collectionValue) {
							builder.append("'");
							builder.append(StringHelper.escapeSql(inValue + ""));
							builder.append("'");
							builder.append(",");
						}
						builder.deleteCharAt(builder.length() - 1);
					} else if (columnType.isEnum()) {
						for (Object inValue : collectionValue) {
							String enumName = ((Enum) inValue).name();
							builder.append("'");
							builder.append(StringHelper.escapeSql(enumName));
							builder.append("'");
							builder.append(",");
						}
						builder.deleteCharAt(builder.length() - 1);
					} else {
						for (Object inValue : collectionValue) {
							builder.append(inValue);
							builder.append(",");
						}
						builder.deleteCharAt(builder.length() - 1);
					}
					builder.append(")");
					sql.where(joint + builder.toString());
				} else if (value instanceof String) {
					String[] inValues = StringUtils.split((String) value, ",");
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					if (columnType.getName().equals(String.class.getName())) {
						for (String inValue : inValues) {
							builder.append("'");
							builder.append(StringHelper.escapeSql(inValue));
							builder.append("'");
							builder.append(",");
						}
					} else {
						for (String inValue : inValues) {
							builder.append(inValue);
							builder.append(",");
						}
					}
					builder.deleteCharAt(builder.length() - 1);
					builder.append(")");
					sql.where(joint + builder.toString());
				} else if (value.getClass().isArray()) {
					String simpleName = value.getClass().getSimpleName();
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					switch (simpleName) {
						case "int[]": {
							int[] values = (int[]) value;
							for (int v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "long[]": {
							long[] values = (long[]) value;
							for (long v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "char[]": {
							char[] values = (char[]) value;
							for (char v : values) {
								builder.append("'");
								builder.append(v);
								builder.append("'");
								builder.append(",");
							}
							break;
						}
						case "double[]": {
							double[] values = (double[]) value;
							for (double v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "byte[]": {
							byte[] values = (byte[]) value;
							for (byte v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "short[]": {
							short[] values = (short[]) value;
							for (short v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "boolean[]": {
							boolean[] values = (boolean[]) value;
							for (boolean v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						case "float[]": {
							float[] values = (float[]) value;
							for (float v : values) {
								builder.append(v);
								builder.append(",");
							}
							break;
						}
						default: {
							Object[] values = (Object[]) value;
							for (Object v : values) {
								builder.append("'");
								builder.append(StringHelper.escapeSql(String.valueOf(v)));
								builder.append("'");
								builder.append(",");
							}
							break;
						}
					}
					builder.deleteCharAt(builder.length() - 1);
					builder.append(")");
					sql.where(joint + builder.toString());
				}
				break;
			case custom:
				sql.where(joint + columnNameAlias + BLANK + value);
				break;
			default:
				break;
		}
	}

	public void buildOrder(Map<String, String> tableAliasMap, AtomicInteger counter, QueryBuilder sql, Table table
			, List<SortProperty> sortProperties, PropertySelector selector) {
		if (CollectionUtils.isNotEmpty(sortProperties)) {
			sortProperties.sort((o1, o2) -> {
				if (o1.getPropertyName() == null) {
					return -1;
				}
				if (o2.getPropertyName() == null) {
					return 1;
				}
				return o1.getPropertyName().compareTo(o2.getPropertyName());
			});
			for (SortProperty sortProperty : sortProperties) {
				String propertyName = sortProperty.getPropertyName();
				if (StringUtils.isBlank(propertyName)) {
					continue;
				}
				String rootAlias = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY);
				//此处的joinType是瞎猜的，不作数
				Column column = resolvePropertyCascade(tableAliasMap, table, rootAlias, counter, sql, JoinType.LEFT, null, propertyName, selector);
				if (column == null) {
					continue;
				}
				String propertyAlias = rootAlias;
				if (propertyName.contains(".")) {
					propertyAlias = tableAliasMap.get(propertyName.substring(0, propertyName.lastIndexOf(".")));
				}
				if (StringUtils.isNotBlank(propertyAlias)) {
					sql.orderBy(propertyAlias + "." + wrap(column.getSqlName()) + BLANK + sortProperty.getSort().name());
				} else {
					sql.orderBy(wrap(column.getSqlName()) + BLANK + sortProperty.getSort().name());
				}
			}
		}
	}

	private String genUniqueParamName(String paramName, Map<String, R2dbcParam> map) {
		if (!map.containsKey(paramName)) {
			return paramName;
		}
		String newParamName = paramName + "_R";
		while (map.containsKey(newParamName)) {
			newParamName += "_R";
		}
		return newParamName;
	}

	private Column resolvePropertyCascade(Map<String, String> tableAliasMap, Table table, String parentTableAlias
			, AtomicInteger counter, QueryBuilder sql, JoinType joinType, String path, String propertyName, PropertySelector selector) {
		if (!propertyName.contains(".")) {
			return table.getColumns().stream().filter(k -> k.getJavaName().equals(propertyName)).findFirst().orElse(null);
		}
		int indexOf = propertyName.indexOf(".");
		String prefix = propertyName.substring(0, indexOf);
		String next = propertyName.substring(indexOf + 1);
		String key = path == null ? prefix : (path + "." + prefix);
		List<Join> joins = table.getJoins();
		if (CollectionUtils.isEmpty(joins)) {
			throw new RuntimeException("error propertyName -> " + key);
		}
		Join join = joins.stream().filter(j -> j.getFieldName().equals(prefix)).findAny().orElse(null);
		if (join == null) {
			throw new RuntimeException("error propertyName -> " + key);
		}

		Class<?> targetEntity = join.getTargetEntity();
		Table targetTable = ORMapping.get(targetEntity);
		assert targetTable != null;
		//done 对分表的支持
		String targetTableName = targetTable.getSqlName();
		String alias = tableAliasMap.computeIfAbsent(key, (k) -> {
			StringBuilder builder = new StringBuilder();
			String targetAlias = createTableAlias(targetTableName, counter);
			builder.append(wrap(targetTableName)).append(BLANK).append("as").append(BLANK).append(targetAlias);
			builder.append(BLANK).append("on").append(BLANK);
			join.getJoinColumns().forEach(joinColumn -> {
				if (StringUtils.isNotBlank(parentTableAlias)) {
					builder.append(BLANK).append(parentTableAlias).append(".").append(wrap(joinColumn.getName())).append(BLANK);
				} else {
					builder.append(BLANK).append(joinColumn.getName()).append(BLANK);
				}
				builder.append("=").append(BLANK).append(targetAlias).append(".").append(wrap(joinColumn.getReferencedColumnName()));
				builder.append(BLANK).append("and");
			});
			builder.delete(builder.length() - 3, builder.length());
			switch (joinType) {
				case INNER:
					sql.innerJoin(builder.toString());
					break;
				case LEFT:
					sql.leftJoin(builder.toString());
					break;
				case RIGHT:
					sql.rightJoin(builder.toString());
					break;
				default:
					break;
			}
			// 根据列选择器动态选择列 增加查询结果
			targetTable.getColumns().stream().filter(column -> {
				if (selector != null) {
					if (selector.getIncludes() != null) {
						return selector.getIncludes().contains(k + "." + column.getJavaName());
					} else if (selector.getExcludes() != null) {
						return !selector.getExcludes().contains(k + "." + column.getJavaName());
					}
				}
				return true;
			}).forEach(column -> {
				sql.select(targetAlias + "." + wrap(column.getSqlName()) + BLANK + "as" + BLANK + "\"" + k + "." + column.getJavaName() + "\"");
			});
			return targetAlias;
		});
		return resolvePropertyCascade(tableAliasMap, targetTable, alias, counter, sql, joinType, key, next, selector);
	}

	private String createTableAlias(String tableName, AtomicInteger counter) {
		return "t" + counter.getAndIncrement();
	}
}
