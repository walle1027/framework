package org.loed.framework.r2dbc.query.dialect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.orm.*;
import org.loed.framework.common.query.*;
import org.loed.framework.common.util.MysqlUtils;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.query.R2dbcQuery;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.GenerationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
		StringBuilder builder = new StringBuilder();
		Map<String, R2dbcParam> params = new HashMap<>();
		builder.append("insert into ").append(wrap(table.getSqlName())).append("(");
		List<Column> columns = table.getColumns().stream().filter(Filters.INSERTABLE_FILTER).collect(Collectors.toList());
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
		return new R2dbcQuery(builder.toString(), params);
	}


	@Override
	public R2dbcQuery batchInsert(List<?> entityList, Table table) {
		StringBuilder builder = new StringBuilder();
		Map<String, R2dbcParam> params = new HashMap<>();
		builder.append("insert into ").append(wrap(table.getSqlName())).append("(");
		List<Column> columns = table.getColumns().stream().filter(Filters.INSERTABLE_FILTER).collect(Collectors.toList());
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
				builder.append(":").append(column.getJavaName()).append("_").append(finalI).append(",");
				params.put(column.getJavaName() + "_" + finalI, new R2dbcParam(column.getJavaType(), ReflectionUtils.getFieldValue(object, column.getJavaName())));
			});
			builder.deleteCharAt(builder.length() - 1).append(")").append(",");
		}
		builder.deleteCharAt(builder.length() - 1);
		if (table.getIdGenerationType() == GenerationType.AUTO) {
			builder.append("; select last_insert_id(),row_count()");
		}
		return new R2dbcQuery(builder.toString(), params);
	}

	@Override
	public R2dbcQuery update(@NonNull Object entity, @NonNull Table table, @NonNull List<Condition> conditions, @NonNull Predicate<Column> columnFilter) {
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(null, table));

		Map<String, R2dbcParam> params = new HashMap<>();
		QueryBuilder builder = new QueryBuilder();
		builder.update(wrap(table.getSqlName()));
		table.getColumns().stream().filter(columnFilter.or(Filters.ALWAYS_UPDATE_FILTER)).forEach(column -> {
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
		if (CollectionUtils.isEmpty(conditions)) {
			log.warn("conditions is empty, the statement:" + builder.toString() + " will update all rows");
		} else {
			buildConditions(builder, params, conditions, tableAliasMap);
		}
		return new R2dbcQuery(builder.toString(), params);
	}

	@Override
	public String rawUpdate(Object entity, Table table, List<Condition> conditions, Predicate<Column> columnFilter) {
		QueryBuilder builder = new QueryBuilder();
		builder.update(wrap(table.getSqlName()));
		table.getColumns().stream().filter(columnFilter.or(Filters.ALWAYS_UPDATE_FILTER)).forEach(column -> {
			StringBuilder setBuilder = new StringBuilder();
			setBuilder.append(wrap(column.getSqlName())).append(BLANK).append("=").append(BLANK);
			if (column.isVersioned()) {
				setBuilder.append(wrap(column.getSqlName())).append(" + 1");
			} else {
				//TODO bind sql
				Object fieldValue = ReflectionUtils.getFieldValue(entity, column.getJavaName());
				Class<?> javaType = column.getJavaType();
				if (fieldValue == null) {
					setBuilder.append(" null ");
				} else {
					setBuilder.append(MysqlUtils.getEscapedSqlVal(fieldValue, column));
				}
			}
			builder.set(setBuilder.toString());
		});
		if (CollectionUtils.isEmpty(builder.getUpdateList())) {
			throw new RuntimeException("empty columns to update");
		}
		if (CollectionUtils.isEmpty(conditions)) {
			log.warn("conditions is empty, the statement:" + builder.toString() + " will update all rows");
		} else {
			//no join or no sub query is simple so,don't use public method
			for (Condition condition : conditions) {
				String propertyName = condition.getPropertyName();
				Column column = table.getColumns().stream().filter(c -> {
					return c.getJavaName().equals(propertyName);
				}).findAny().get();
				Object value = condition.getValue();
				StringBuilder where = new StringBuilder();
				where.append(propertyName).append(BLANK).append(condition.getOperator().value()).append(BLANK);
				if (value == null) {
					where.append("null");
				} else {
					where.append(MysqlUtils.getEscapedSqlVal(value, column));
				}
				builder.where(where.toString());
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(builder.toString());
		}
		return builder.toString();
	}

	@Override
	public R2dbcQuery delete(@NonNull Table table, @NonNull List<Condition> conditions, @Nullable SystemContext systemContext) {
		QueryBuilder builder = new QueryBuilder();
		Map<String, R2dbcParam> paramMap = new HashMap<>();
		Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(null, table));
		//check has is_deleted column
		Column isDeletedColumn = table.getColumns().stream().filter(Column::isDeleted).findFirst().orElse(null);
		if (isDeletedColumn != null) {
			builder.update(wrap(table.getSqlName()));
			//判断是否有版本号
			Optional<Column> versionColumn = table.getColumns().stream().filter(Column::isVersioned).findAny();
			if (versionColumn.isPresent()) {
				Column column = versionColumn.get();
				builder.set(wrap(column.getSqlName()) + BLANK + "=" + BLANK + wrap(column.getSqlName()) + " + 1");
			}
			//判断是否有最后更新人
			Optional<Column> isLastModifyByColumn = table.getColumns().stream().filter(Column::isLastModifyBy).findAny();
			if (isLastModifyByColumn.isPresent()) {
				Column column = isLastModifyByColumn.get();
				builder.set(wrap(column.getSqlName()) + BLANK + "= :lastModifyBy");
				paramMap.put("lastModifyBy", (systemContext != null && systemContext.getUserId() != null)
						? new R2dbcParam(column.getJavaType(), DataType.toType(systemContext.getUserId(), DataType.getDataType(column.getJavaType())))
						: new R2dbcParam(column.getJavaType(), null));
			}
			Optional<Column> isLastModifyTimeColumn = table.getColumns().stream().filter(Column::isLastModifyTime).findAny();
			if (isLastModifyTimeColumn.isPresent()) {
				Column column = isLastModifyTimeColumn.get();
				Class<?> type = column.getJavaType();
				if (type.getName().equals(LocalDate.class.getName())) {
					builder.set(wrap(column.getSqlName()) + BLANK + "= :isLastModifyTime");
					paramMap.put("isLastModifyTime", new R2dbcParam(LocalDate.class, LocalDate.now()));
				} else if (type.getName().equals(LocalDateTime.class.getName())) {
					builder.set(wrap(column.getSqlName()) + BLANK + "= :isLastModifyTime");
					paramMap.put("isLastModifyTime", new R2dbcParam(LocalDateTime.class, LocalDateTime.now()));
				} else {
					log.error("unsupported type :" + table.getJavaName() + "#" + column.getJavaName() + "(" + column.getJavaType() + ")" + " for isLastModifyTime column ,will not set any value");
				}
			}
			builder.set(wrap(isDeletedColumn.getSqlName()) + BLANK + "=" + BLANK + "1");
			//判断是否有最后更新人和最后更新时间
		} else {
			builder.delete(wrap(table.getSqlName()));
		}
		if (CollectionUtils.isEmpty(conditions)) {
			log.warn("criteria is empty condition");
		} else {
			buildConditions(builder, paramMap, conditions, tableAliasMap);
		}
		return new R2dbcQuery(builder.toString(), paramMap);
	}

	private void buildConditions(QueryBuilder builder, Map<String, R2dbcParam> paramMap, List<Condition> conditions, Map<String, TableWithAlias> tableAliasMap) {
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
			buildCondition(builder, paramMap, tableAliasMap, condition);
		}
	}

	@Override
	public <T> R2dbcQuery find(@NonNull Table table, @NonNull Criteria<T> criteria) {
		QueryBuilder builder = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> paramMap = new HashMap<>();
		String tableName = table.getSqlName();
		Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(tableName, counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(rootAlias, table));
		PropertySelector selector = criteria.getSelector();
		//build select items
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
		//build from clause
		builder.from(wrap(tableName) + " as " + rootAlias);
		//build joins
		TreeMap<String, Join> joins = criteria.getJoins();
		if (joins != null && !joins.isEmpty()) {
			for (Join join : joins.values()) {
				buildJoinSequential(tableAliasMap, counter, builder, join, selector);
			}
		}
		// build conditions
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			buildConditions(builder, paramMap, conditions, tableAliasMap);
		}
		//build orders
		buildOrder(tableAliasMap, builder, criteria.getSortProperties());
		if (log.isDebugEnabled()) {
			log.debug(builder.toString());
		}
		return new R2dbcQuery(builder.toString(), paramMap);
	}

//	@Override
//	public <T> R2dbcQuery findPage(@NonNull Table table, @NonNull Criteria<T> criteria, @NonNull PageRequest pageRequest) {
//		R2dbcQuery query = find(table, criteria);
//		String statement = query.getStatement();
//		Map<String, R2dbcParam> params = query.getParams();
//		if (params == null) {
//			params = new HashMap<>();
//		}
//		String prstmt;
//		if (pageRequest.getPageNumber() > 0) {
//			prstmt = statement + BLANK + "limit :pr_limit offset :pr_offset";
//			params.put("pr_limit", new R2dbcParam(Integer.class, pageRequest.getPageSize()));
//			params.put("pr_offset", new R2dbcParam(Long.class, pageRequest.getOffset()));
//		} else {
//			prstmt = statement + BLANK + "limit :pr_limit";
//			params.put("pr_limit", new R2dbcParam(Integer.class, pageRequest.getPageSize()));
//		}
//		return new R2dbcQuery(prstmt, params);
//	}

	@Override
	public <T> R2dbcQuery findPage(Table table, Criteria<T> criteria, int limit, long offset) {
		R2dbcQuery query = find(table, criteria);
		String statement = query.getStatement();
		Map<String, R2dbcParam> params = query.getParams();
		if (params == null) {
			params = new HashMap<>();
		}
		String prstmt;
		if (offset > 0) {
			prstmt = statement + BLANK + "limit :pr_limit offset :pr_offset";
			params.put("pr_limit", new R2dbcParam(Integer.class, limit));
			params.put("pr_offset", new R2dbcParam(Long.class, offset));
		} else {
			prstmt = statement + BLANK + "limit :pr_limit";
			params.put("pr_limit", new R2dbcParam(Integer.class, limit));
		}
		return new R2dbcQuery(prstmt, params);
	}

	@Override
	public <T> R2dbcQuery count(@NonNull Table table, @NonNull Criteria<T> criteria) {
		QueryBuilder builder = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, R2dbcParam> paramMap = new HashMap<>();
		String tableName = table.getSqlName();
		Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(tableName, counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(rootAlias, table));
		//build count clause
		builder.select("count(1)").from(wrap(tableName) + " as " + rootAlias);

		List<Condition> conditions = criteria.getConditions();
		PropertySelector selector = criteria.getSelector();
		//build joins
		TreeMap<String, Join> joins = criteria.getJoins();
		if (joins != null && !joins.isEmpty()) {
			for (Join join : joins.values()) {
				buildJoinSequential(tableAliasMap, counter, builder, join, selector);
			}
		}
		//build conditions
		if (CollectionUtils.isNotEmpty(conditions)) {
			buildConditions(builder, paramMap, conditions, tableAliasMap);
		}
		// do not include order by clause
		if (log.isDebugEnabled()) {
			log.debug(builder.toString());
		}
		return new R2dbcQuery(builder.toString(), paramMap);
	}

	@Override
	public boolean isQuote() {
		return this.quote;
	}

	@Override
	public String quote() {
		return "`";
	}


	private void buildJoinSequential(Map<String, TableWithAlias> tableAliasMap, AtomicInteger counter, QueryBuilder sql, Join join, PropertySelector selector) {
		String uniquePath = join.getUniquePath();
		String target = join.getTarget();
		String parentAlias;
		Table parentTable;
		if (!StringUtils.contains(uniquePath, Condition.PATH_SEPARATOR)) {
			parentAlias = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY).alias;
			parentTable = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY).table;
		} else {
			String parentPath = uniquePath.substring(0, uniquePath.lastIndexOf(Condition.PATH_SEPARATOR));
			parentAlias = tableAliasMap.get(parentPath).alias;
			parentTable = tableAliasMap.get(parentPath).table;
		}
		if (parentTable == null || parentAlias == null) {
			throw new RuntimeException("error property path for join " + join.toString());
		}
		JoinTable joinTable = parentTable.getJoinTables().stream().filter(jt -> {
			return jt.getFieldName().equals(target);
		}).findFirst().orElse(null);
		if (joinTable == null) {
			throw new RuntimeException("error join property -> " + uniquePath);
		}
		Class<?> targetEntity = joinTable.getTargetEntity();
		Table targetTable = ORMapping.get(targetEntity);
		if (targetTable == null) {
			throw new RuntimeException("error relationship for entity " + parentTable.getJavaName() + " -> " + joinTable.getFieldName());
		}
		String targetTableName = getTableNameByCriteria(targetTable, null);
		String targetAlias = createTableAlias(targetTableName, counter);
		StringBuilder builder = new StringBuilder();
		builder.append(wrap(targetTableName)).append(BLANK).append("as").append(BLANK).append(targetAlias);
		builder.append(BLANK).append("on").append(BLANK);
		String joins = joinTable.getJoinColumns().stream().map(joinColumn -> {
			StringBuilder joinBuilder = new StringBuilder();
			joinBuilder.append(BLANK);
			if (StringUtils.isNotBlank(parentAlias)) {
				joinBuilder.append(parentAlias).append(".");
			}
			joinBuilder.append(wrap(joinColumn.getName()));
			joinBuilder.append(BLANK).append("=").append(BLANK);
			joinBuilder.append(targetAlias).append(".").append(wrap(joinColumn.getReferencedColumnName()));
			joinBuilder.append(BLANK);
			return joinBuilder.toString();
		}).collect(Collectors.joining("and"));
		builder.append(joins);
		switch (join.getJoinType()) {
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
		if (sql.getStatementType() == QueryBuilder.StatementType.select) {
			// 根据列选择器动态选择列 增加查询结果
			targetTable.getColumns().stream().filter(column -> {
				if (selector != null) {
					if (selector.getIncludes() != null) {
						return selector.getIncludes().contains(uniquePath + "." + column.getJavaName());
					} else if (selector.getExcludes() != null) {
						return !selector.getExcludes().contains(uniquePath + "." + column.getJavaName());
					}
				}
				return true;
			}).forEach(column -> {
				sql.select(targetAlias + "." + wrap(column.getSqlName()) + BLANK + "as" + BLANK + "\"" + uniquePath + "." + column.getJavaName() + "\"");
			});
		}

		TableWithAlias tableWithAlias = new TableWithAlias(targetAlias, targetTable);
		tableAliasMap.put(uniquePath, tableWithAlias);
	}


	public void buildCondition(QueryBuilder builder, Map<String, R2dbcParam> parameterMap, Map<String, TableWithAlias> tableAliasMap, Condition condition) {
		if (condition.hasSubCondition()) {
			if (condition.getJoint() != null) {
				builder.where(condition.getJoint().name() + BLANK + "(");
			} else {
				builder.where("(");
			}
			for (Condition subCondition : condition.getSubConditions()) {
				buildCondition(builder, parameterMap, tableAliasMap, subCondition);
			}
			builder.where(")");
		} else {
			buildSingleCondition(builder, parameterMap, tableAliasMap, condition);
		}
	}

	private void buildSingleCondition(QueryBuilder sql, Map<String, R2dbcParam> parameterMap, Map<String, TableWithAlias> tableAliasMap, Condition condition) {
		if (!match(condition)) {
			log.warn("condition :" + condition + " is not a valid condition,will ignore");
			return;
		}
		String propertyName = condition.getPropertyName();
		String joint = (condition.getJoint() == null ? "" : condition.getJoint().name()) + BLANK;
		Object value = condition.getValue();
		String rawParamName = StringUtils.replace(propertyName, ".", "_") + "Value";
		String uniqueParamName = genUniqueParamName(rawParamName, parameterMap);

		ColumnWithAlias resolveProperty = resolvePropertyCascade(tableAliasMap, propertyName);
		Column column = resolveProperty.column;
		String alias = resolveProperty.alias;
		String columnName = column.getSqlName();
		Class<?> columnType = column.getJavaType();
		int dataType = DataType.getDataType(columnType);
		String columnNameAlias;
		if (StringUtils.isBlank(alias)) {
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
					if (((Collection<?>) value).size() < 2) {
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
							String enumName = ((Enum<?>) inValue).name();
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

	public void buildOrder(Map<String, TableWithAlias> tableAliasMap, QueryBuilder builder, List<SortProperty> sortProperties) {
		if (CollectionUtils.isEmpty(sortProperties)) {
			return;
		}
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
			ColumnWithAlias columnWithAlias = resolvePropertyCascade(tableAliasMap, propertyName);
			String alias = columnWithAlias.alias;
			Column column = columnWithAlias.column;
			if (StringUtils.isNotBlank(alias)) {
				builder.orderBy(alias + "." + wrap(column.getSqlName()) + BLANK + sortProperty.getSort().name());
			} else {
				builder.orderBy(wrap(column.getSqlName()) + BLANK + sortProperty.getSort().name());
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

	private ColumnWithAlias resolvePropertyCascade(Map<String, TableWithAlias> tableAliasMap, String propertyName) {
		String parentPath;
		String propName;
		if (!propertyName.contains(Condition.PATH_SEPARATOR)) {
			parentPath = ROOT_TABLE_ALIAS_KEY;
			propName = propertyName;
		} else {
			int index = propertyName.lastIndexOf(".");
			parentPath = propertyName.substring(0, index);
			propName = propertyName.substring(index + 1);

		}
		TableWithAlias tableWithAlias = tableAliasMap.get(parentPath);
		if (tableWithAlias == null) {
			throw new RuntimeException("property path :" + propertyName + " has no joins in this criteria");
		}
		Table table = tableWithAlias.table;
		Column column = table.getColumns().stream().filter(k -> k.getJavaName().equals(propName)).findFirst().orElse(null);
		if (column == null) {
			throw new RuntimeException("can't resolve property :" + propertyName + " from table " + table);
		}
		return new ColumnWithAlias(tableWithAlias.alias, column);
	}

	private static class TableWithAlias {
		private final String alias;
		private final Table table;

		public TableWithAlias(String alias, Table table) {
			this.alias = alias;
			this.table = table;
		}
	}

	private static class ColumnWithAlias {
		private final String alias;
		private final Column column;

		public ColumnWithAlias(String alias, Column column) {
			this.alias = alias;
			this.column = column;
		}
	}
}
