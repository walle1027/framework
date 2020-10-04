package org.loed.framework.mybatis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.JdbcType;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.orm.Filters;
import org.loed.framework.common.orm.*;
import org.loed.framework.common.query.*;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.mybatis.sharding.ShardingManager;
import org.loed.framework.mybatis.sharding.table.po.IdMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/2 下午3:50
 */
@SuppressWarnings({"unused", "Duplicates"})
public class MybatisSqlBuilder {
	public static final String BLANK = " ";
	private static final String ROOT_TABLE_ALIAS_KEY = "_self";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final Logger logger = LoggerFactory.getLogger(MybatisSqlBuilder.class);

	public void buildCondition(Map<String, Object> parameterMap, Map<String, TableWithAlias> tableAliasMap, QueryBuilder sql, Condition condition) {
		if (condition.hasSubCondition()) {
			if (condition.getJoint() != null) {
				sql.where(condition.getJoint().name() + BLANK + "(");
			} else {
				sql.where("(");
			}
			for (Condition subCondition : condition.getSubConditions()) {
				buildCondition(parameterMap, tableAliasMap, sql, subCondition);
			}
			sql.where(")");
		} else {
			buildSingleCondition(parameterMap, tableAliasMap, sql, condition);
		}
	}

	private void buildSingleCondition(Map<String, Object> parameterMap, Map<String, TableWithAlias> tableAliasMap, QueryBuilder sql, Condition condition) {
		if (!match(condition)) {
			return;
		}
		String propertyName = condition.getPropertyName();
		String joint = (condition.getJoint() == null ? "" : condition.getJoint().name()) + BLANK;
		Object value = condition.getValue();
		Operator operator = condition.getOperator();
		String rawParamName = StringUtils.replace(propertyName, ".", "_") + "Value";
		String uniqueParamName = genUniqueMapKey(rawParamName, parameterMap);
		ColumnWithAlias columnWithAlias = resolvePropertyCascade(tableAliasMap, propertyName);
		Column column = columnWithAlias.column;
		String alias = columnWithAlias.alias;

		String columnName = column.getSqlName();
		String jdbcType = column.getSqlTypeName();
		int dataType = DataType.getDataType(column.getJavaType());
		String columnNameAlias;
		if (alias == null) {
			columnNameAlias = columnName;
		} else {
			columnNameAlias = alias + "." + columnName;
		}

		switch (condition.getOperator()) {
			case beginWith:
			case notBeginWith:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + "#{map." + uniqueParamName + ",jdbcType=" + jdbcType + "}");
				parameterMap.put(uniqueParamName, value + "%");
				break;
			case endWith:
			case notEndWith:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + "#{map." + uniqueParamName + ",jdbcType=" + jdbcType + "}");
				parameterMap.put(uniqueParamName, "%" + value);
				break;
			case contains:
			case notContains:
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + "#{map." + uniqueParamName + ",jdbcType=" + jdbcType + "}");
				parameterMap.put(uniqueParamName, "%" + value + "%");
				break;
			case between:
			case notBetween:
				String betweenKey1 = genUniqueMapKey(uniqueParamName + "Value1", parameterMap);
				String betweenKey2 = genUniqueMapKey(uniqueParamName + "Value2", parameterMap);
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + "#{map." + betweenKey1 + ",jdbcType=" + jdbcType + "}" + BLANK
						+ "and" + BLANK + "#{map." + betweenKey2 + ",jdbcType=" + jdbcType + "}");
				if (value instanceof Collection) {
					int i = 0;
					for (Object v : (Collection<?>) value) {
						if (i == 0) {
							parameterMap.put(betweenKey1, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
						}
						if (i == 1) {
							parameterMap.put(betweenKey2, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
						}
						i++;
					}
				} else if (value instanceof String) {
					String[] betweenValues = StringUtils.split((String) value, ",");
					parameterMap.put(betweenKey1, DataType.toType(betweenValues[0], DataType.DT_String, dataType));
					if (betweenValues.length > 1) {
						parameterMap.put(betweenKey2, DataType.toType(betweenValues[1], DataType.DT_String, dataType));
					} else {
						parameterMap.put(betweenKey2, null);
					}
				} else if (value.getClass().isArray()) {
					String simpleName = value.getClass().getSimpleName();
					Object[] values = (Object[]) value;
					for (int i = 0; i < values.length; i++) {
						Object v = values[i];
						if (i == 0) {
							parameterMap.put(betweenKey1, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
						}
						if (i == 1) {
							parameterMap.put(betweenKey2, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
						}
					}
//					switch (simpleName) {
//						case "int[]": {
//							int[] values = (int[]) value;
//							for (int i = 0; i < values.length; i++) {
//								int v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_int, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_int, dataType));
//								}
//							}
//							break;
//						}
//						case "long[]": {
//							long[] values = (long[]) value;
//							for (int i = 0; i < values.length; i++) {
//								long v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_long, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_long, dataType));
//								}
//							}
//							break;
//						}
//						case "char[]": {
//							char[] values = (char[]) value;
//							for (int i = 0; i < values.length; i++) {
//								char v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_char, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_char, dataType));
//								}
//							}
//							break;
//						}
//						case "double[]": {
//							double[] values = (double[]) value;
//							for (int i = 0; i < values.length; i++) {
//								double v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_double, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_double, dataType));
//								}
//							}
//							break;
//						}
//						case "byte[]": {
//							byte[] values = (byte[]) value;
//							for (int i = 0; i < values.length; i++) {
//								byte v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_byte, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_byte, dataType));
//								}
//							}
//							break;
//						}
//						case "short[]": {
//							short[] values = (short[]) value;
//							for (int i = 0; i < values.length; i++) {
//								short v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_short, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_short, dataType));
//								}
//							}
//							break;
//						}
//						case "boolean[]": {
//							boolean[] values = (boolean[]) value;
//							for (int i = 0; i < values.length; i++) {
//								boolean v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_boolean, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_boolean, dataType));
//								}
//							}
//							break;
//						}
//						case "float[]": {
//							float[] values = (float[]) value;
//							for (int i = 0; i < values.length; i++) {
//								float v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_float, dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_float, dataType));
//								}
//							}
//							break;
//						}
//						default: {
//							Object[] values = (Object[]) value;
//							for (int i = 0; i < values.length; i++) {
//								Object v = values[i];
//								if (i == 0) {
//									parameterMap.put(betweenKey1, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
//								}
//								if (i == 1) {
//									parameterMap.put(betweenKey2, DataType.toType(v, DataType.getDataType(v.getClass()), dataType));
//								}
//							}
//							break;
//						}
//					}
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
				sql.where(joint + columnNameAlias + BLANK + condition.getOperator().value() + BLANK + "#{map." + uniqueParamName + ",jdbcType=" + jdbcType + "}");
				parameterMap.put(uniqueParamName, DataType.toType(value, dataType));
				break;
			case in:
			case notIn:
				if (value instanceof Collection) {
					Collection<?> collectionValue = (Collection<?>) value;
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					//todo 改为参数注入
					for (Object inValue : collectionValue) {
						if (inValue.getClass().getName().equals(String.class.getName())) {
							builder.append("'");
							builder.append(StringHelper.escapeSql(inValue + ""));
							builder.append("'");
							builder.append(",");
						} else {
							builder.append(inValue);
							builder.append(",");
						}
					}

					builder.deleteCharAt(builder.length() - 1);
					builder.append(")");
					sql.where(joint + builder.toString());
				} else if (value instanceof String) {
					String[] inValues = StringUtils.split((String) value, ",");
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					if (jdbcType.equals(JdbcType.VARCHAR.toString())) {
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
//					String simpleName = value.getClass().getSimpleName();
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					Object[] values = (Object[]) value;
					for (Object v : values) {
						if (v.getClass().getName().equals(String.class.getName())) {
							builder.append("'");
							builder.append(StringHelper.escapeSql(String.valueOf(v)));
							builder.append("'");
							builder.append(",");
						} else {
							builder.append(v);
							builder.append(",");
						}
					}
//					switch (simpleName) {
//						case "int[]": {
//							int[] values = (int[]) value;
//							for (int v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "long[]": {
//							long[] values = (long[]) value;
//							for (long v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "char[]": {
//							char[] values = (char[]) value;
//							for (char v : values) {
//								builder.append("'");
//								builder.append(v);
//								builder.append("'");
//								builder.append(",");
//							}
//							break;
//						}
//						case "double[]": {
//							double[] values = (double[]) value;
//							for (double v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "byte[]": {
//							byte[] values = (byte[]) value;
//							for (byte v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "short[]": {
//							short[] values = (short[]) value;
//							for (short v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "boolean[]": {
//							boolean[] values = (boolean[]) value;
//							for (boolean v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						case "float[]": {
//							float[] values = (float[]) value;
//							for (float v : values) {
//								builder.append(v);
//								builder.append(",");
//							}
//							break;
//						}
//						default: {
//							Object[] values = (Object[]) value;
//							for (Object v : values) {
//								builder.append("'");
//								builder.append(StringHelper.escapeSql(String.valueOf(v)));
//								builder.append("'");
//								builder.append(",");
//							}
//							break;
//						}
//					}
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
//		String targetTableName = getTableNameByCriteria(tagetTable, null);
		//todo consider join tables is a sharding table
		String targetTableName = targetTable.getSqlName();
		String targetAlias = createTableAlias(targetTableName, counter);
		StringBuilder builder = new StringBuilder();
		builder.append(targetTableName).append(BLANK).append("as").append(BLANK).append(targetAlias);
		builder.append(BLANK).append("on").append(BLANK);
		String joins = joinTable.getJoinColumns().stream().map(joinColumn -> {
			StringBuilder joinBuilder = new StringBuilder();
			joinBuilder.append(BLANK);
			if (StringUtils.isNotBlank(parentAlias)) {
				joinBuilder.append(parentAlias).append(".");
			}
			joinBuilder.append(joinColumn.getName());
			joinBuilder.append(BLANK).append("=").append(BLANK);
			joinBuilder.append(targetAlias).append(".").append(joinColumn.getReferencedColumnName());
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
				sql.select(targetAlias + "." + column.getSqlName() + BLANK + "as" + BLANK + "\"" + uniquePath + "." + column.getJavaName() + "\"");
			});
		}
		TableWithAlias tableWithAlias = new TableWithAlias(targetAlias, targetTable);
		tableAliasMap.put(uniquePath, tableWithAlias);
	}

	private ColumnWithAlias resolvePropertyCascade(Map<String, TableWithAlias> tableAliasMap, String propertyName) {
		String propertyPath;
		String property;
		if (propertyName.contains(Condition.PATH_SEPARATOR)) {
			int index = propertyName.lastIndexOf(Condition.PATH_SEPARATOR);
			propertyPath = propertyName.substring(0, index);
			property = propertyName.substring(index + 1);
		} else {
			propertyPath = ROOT_TABLE_ALIAS_KEY;
			property = propertyName;
		}
		TableWithAlias tableWithAlias = tableAliasMap.get(propertyPath);
		if (tableWithAlias == null) {
			logger.error("error property path :" + propertyName);
			throw new RuntimeException("error property path :" + propertyName);
		}
		Table table = tableWithAlias.table;
		Column column = table.getColumns().stream().filter(k -> k.getJavaName().equals(property)).findFirst().orElse(null);
		if (column == null) {
			throw new RuntimeException("property:" + property + " no found in class:" + table.getJavaName());
		}
		return new ColumnWithAlias(tableWithAlias.alias, column);
	}

	private String genUniqueMapKey(String mapKey, Map<String, Object> map) {
		if (!map.containsKey(mapKey)) {
			return mapKey;
		}
		String newKey = mapKey + "_R";
		while (map.containsKey(newKey)) {
			newKey += "_R";
		}
		return newKey;
	}

	private void buildOrder(Map<String, TableWithAlias> tableAliasMap, QueryBuilder sql, List<SortProperty> sortProperties) {
		if (CollectionUtils.isNotEmpty(sortProperties)) {
			for (SortProperty sortProperty : sortProperties) {
				String propertyName = sortProperty.getPropertyName();
				if (StringUtils.isBlank(propertyName)) {
					continue;
				}
				ColumnWithAlias columnWithAlias = resolvePropertyCascade(tableAliasMap, propertyName);
				String alias = columnWithAlias.alias;
				Column column = columnWithAlias.column;
				if (StringUtils.isNotBlank(alias)) {
					sql.orderBy(alias + "." + column.getSqlName() + BLANK + sortProperty.getSort().name());
				} else {
					sql.orderBy(column.getSqlName() + BLANK + sortProperty.getSort().name());
				}
			}
		}
	}

	public boolean match(Condition condition) {
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

	private String createTableAlias(String tableName, AtomicInteger counter) {
		return "t" + counter.getAndIncrement();
	}

	public String update(@Param("po") Object object, @Param("columnFilter") Predicate<Column> predicate) {
		Class<?> poClass = object.getClass();
		Table table = ORMapping.get(poClass);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + poClass.getName());
		}
		StringBuilder builder = new StringBuilder();
		builder.append("update");
		builder.append(BLANK);
		Serializable idValue = getIdValue(table, object);
		builder.append(getTableNameById(table, idValue));
		builder.append(BLANK).append("set").append(BLANK);
		String set = table.getColumns().stream().filter(predicate.or(Filters.VERSION_FILTER).and(Filters.ID_FILTER.negate())).map(column -> {
			StringBuilder columnBuilder = new StringBuilder();
			if (column.isVersioned()) {
				columnBuilder.append(column.getSqlName()).append(BLANK).append("=").append(BLANK)
						.append(column.getSqlName()).append(" + 1");
			} else {
				columnBuilder.append(BLANK).append(column.getSqlName());
				columnBuilder.append("=");
				columnBuilder.append("#{").append("po.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
			}
			return columnBuilder.toString();
		}).collect(Collectors.joining(","));
		builder.append(set).append(BLANK);

		builder.append(BLANK).append("where").append(BLANK);
		AtomicInteger pkIndex = new AtomicInteger(0);
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			if (pkIndex.get() > 0) {
				builder.append(BLANK).append("and").append(BLANK);
			}
			builder.append(column.getSqlName()).append(" = #{po.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
			pkIndex.getAndIncrement();
		});
		if (logger.isDebugEnabled()) {
			logger.debug(builder.toString());
		}
		return builder.toString();
	}

	public <T> String deleteByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}

		AtomicInteger counter = new AtomicInteger(1);
		Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(null, table));

		Optional<Column> isDeleted = table.getColumns().stream().filter(Column::isDeleted).findAny();
		Set<String> tables = getTableNameByCriteria(table, criteria);
		//if table is sharding and this conditions contains sharding columns ,will delete or update all sharding tables
		String sql = tables.stream().map(tableName -> {
			QueryBuilder query = new QueryBuilder();
			if (isDeleted.isPresent()) {
				query.update(tableName);
				query.set(isDeleted.get().getSqlName() + " = 1");
				Optional<Column> isVersioned = table.getColumns().stream().filter(Column::isVersioned).findAny();
				isVersioned.ifPresent(column -> {
					query.set(column.getSqlName() + " = " + column.getSqlName() + " + 1");
				});
				Optional<Column> isLastModifyBy = table.getColumns().stream().filter(Column::isLastModifyBy).findAny();
				isLastModifyBy.ifPresent(column -> {
					query.set(column.getSqlName() + BLANK + "= #{map.lastModifyBy:" + column.getSqlTypeName() + "}");
					Class<?> type = column.getJavaType();
					if (type.getName().equals(String.class.getName())) {
						map.putIfAbsent("lastModifyBy", SystemContextHolder.getUserId());
					} else if (type.getName().equals(Long.class.getName()) || type.getName().equals(long.class.getName())) {
						map.putIfAbsent("lastModifyBy", Long.valueOf(SystemContextHolder.getUserId()));
					} else if (type.getName().equals(BigInteger.class.getName())) {
						map.putIfAbsent("lastModifyBy", BigInteger.valueOf(Long.parseLong(SystemContextHolder.getUserId())));
					}
				});
				Optional<Column> isLastModifyTime = table.getColumns().stream().filter(Column::isLastModifyTime).findAny();
				isLastModifyTime.ifPresent(column -> {
					query.set(column.getSqlName() + BLANK + "= #{map.lastModifyTime:" + column.getSqlTypeName() + "}");
					//TODO check date type
					Class<?> type = column.getJavaType();
					if (type.getName().equals(Date.class.getName())) {
						map.putIfAbsent("lastModifyTime", new Date());
					} else if (type.getName().equals(java.sql.Date.class.getName())) {
						map.putIfAbsent("lastModifyTime", new java.sql.Date(System.currentTimeMillis()));
					} else if (type.getName().equals(LocalDateTime.class.getName())) {
						map.putIfAbsent("lastModifyTime", LocalDateTime.now());
					}
				});
			} else {
				query.delete(tableName);
			}

			List<Condition> conditions = criteria.getConditions();
			if (CollectionUtils.isNotEmpty(conditions)) {
				for (Condition condition : conditions) {
					buildCondition(map, tableAliasMap, query, condition);
				}
			}
			return query.toString();
		}).collect(Collectors.joining(";"));

		if (logger.isDebugEnabled()) {
			logger.debug(sql);
		}
		return sql;
	}

	public <T> String findByCriteria(@Param("clazz") Class<T> clazz, @Param("criteria") Criteria<T> criteria, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		AtomicInteger counter = new AtomicInteger(1);
		Set<String> tables = getTableNameByCriteria(table, criteria);
		PropertySelector selector = criteria.getSelector();
		List<Condition> conditions = criteria.getConditions();
		String sql = tables.stream().map(tableName -> {
			QueryBuilder builder = new QueryBuilder();
			Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
			String rootAlias = createTableAlias(tableName, counter);
			tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(rootAlias, table));
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
				builder.select(rootAlias + "." + column.getSqlName() + " as " + "\"" + column.getJavaName() + "\"");
			});
			//build from clause
			builder.from(tableName + " as " + rootAlias);
			//build joins
			TreeMap<String, Join> joins = criteria.getJoins();
			if (joins != null && !joins.isEmpty()) {
				for (Map.Entry<String, Join> entry : joins.entrySet()) {
					buildJoinSequential(tableAliasMap, counter, builder, entry.getValue(), selector);
				}
			}
			//build conditions
			if (CollectionUtils.isNotEmpty(conditions)) {
				for (Condition condition : conditions) {
					buildCondition(map, tableAliasMap, builder, condition);
				}
			}
			//build order by clause
			buildOrder(tableAliasMap, builder, criteria.getSortProperties());
			return builder.toString();
		}).collect(Collectors.joining(LINE_SEPARATOR + " union all " + LINE_SEPARATOR));

		if (logger.isDebugEnabled()) {
			logger.debug(sql);
		}
		return sql;
	}

	public String countByCriteria(@Param("clazz") Class<?> clazz, @Param("criteria") Criteria<?> criteria, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		//支持分表的查询
		AtomicInteger counter = new AtomicInteger(1);
		Set<String> tableNames = getTableNameByCriteria(table, criteria);
		List<Condition> conditions = criteria.getConditions();
		PropertySelector selector = criteria.getSelector();
		TreeMap<String, Join> joins = criteria.getJoins();
		if (tableNames.size() > 1) {
			String statement = tableNames.stream().map(tableName -> {
				QueryBuilder sql = new QueryBuilder();
				Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
				String rootAlias = createTableAlias(tableName, counter);
				tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(rootAlias, table));
				sql.select("count(1)");
				sql.from(tableName + BLANK + "as" + BLANK + rootAlias);
				if (joins != null && !joins.isEmpty()) {
					for (Map.Entry<String, Join> entry : joins.entrySet()) {
						buildJoinSequential(tableAliasMap, counter, sql, entry.getValue(), selector);
					}
				}
				if (CollectionUtils.isNotEmpty(conditions)) {
					for (Condition condition : conditions) {
						buildCondition(map, tableAliasMap, sql, condition);
					}
				}
				return sql.toString();
			}).map(sql -> " ( " + sql + " ) ").collect(Collectors.joining(" + "));
			if (logger.isDebugEnabled()) {
				logger.debug("select " + statement);
			}
			return "select " + statement;
		} else {
			String tableName = tableNames.stream().findFirst().get();
			QueryBuilder sql = new QueryBuilder();
			Map<String, TableWithAlias> tableAliasMap = new ConcurrentHashMap<>();
			String rootAlias = createTableAlias(tableName, counter);
			tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, new TableWithAlias(rootAlias, table));
			sql.select("count(1)");
			sql.from(tableName + BLANK + "as" + BLANK + rootAlias);
			if (joins != null && !joins.isEmpty()) {
				for (Map.Entry<String, Join> entry : joins.entrySet()) {
					buildJoinSequential(tableAliasMap, counter, sql, entry.getValue(), selector);
				}
			}
			if (CollectionUtils.isNotEmpty(conditions)) {
				for (Condition condition : conditions) {
					buildCondition(map, tableAliasMap, sql, condition);
				}
			}
			return sql.toString();
		}
	}

	public String sql(@Param("sql") String sql) {
		return sql;
	}

//	public void buildSelectItem(PropertySelector selector, Table table, StringBuilder builder) {
//		table.getColumns().stream().filter(column -> {
//			if (selector != null) {
//				if (selector.getIncludes() != null) {
//					return selector.getIncludes().contains(column.getJavaName());
//				} else if (selector.getExcludes() != null) {
//					return !selector.getExcludes().contains(column.getJavaName());
//				}
//			}
//			return true;
//		}).forEach(column -> {
//			builder.append(BLANK).append(column.getSqlName()).append(BLANK)
//					.append("as").append(BLANK)
//					.append("\"").append(column.getJavaName()).append("\"");
//			builder.append(",");
//		});
//		builder.deleteCharAt(builder.length() - 1);
//	}

	public String shardingGetList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<?> clazz) {
		return BatchType.BatchGetList.name();
	}

	public String shardingGetByIdList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<?> clazz) {
		return BatchType.BatchGetByIdList.name();
	}

	private String getTableNameById(Table table, Serializable idValue) {
		if (!table.isSharding()) {
			return table.getSqlName();
		}
		ShardingManager shardingManager = getShardingManager();
		return shardingManager.getShardingTableNameById(table, idValue);
	}

	public static String getTableNameByPO(Table table, Object object) {
		if (!table.isSharding()) {
			return table.getSqlName();
		}
		List<Column> shardingColumns = table.getColumns().stream().filter(Column::isShardingColumn).sorted(Comparator.comparing(Column::getJavaName)).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(shardingColumns)) {
			throw new RuntimeException("empty sharding columns for table:" + table);
		}
		List<String> shardingValues = new ArrayList<>();
		shardingColumns.forEach(column -> {
			String javaName = column.getJavaName();
			Object fieldValue = ReflectionUtils.getFieldValue(object, javaName);
			if (fieldValue == null) {
				throw new RuntimeException("null value for sharding :" + javaName);
			}
			shardingValues.add(String.valueOf(fieldValue));
		});
		String shardingValue = getShardingValue(shardingValues);
		String shardingTableName = getShardingManager().getShardingTableNameByValue(table, shardingValue);
		IdMapping idMapping = new IdMapping();
		Serializable idValue = getIdValue(table, object);
		idMapping.setId(idValue);
		idMapping.setShardingKey(shardingColumns.stream().map(Column::getSqlName).collect(Collectors.joining(",")));
		idMapping.setShardingValue(shardingValue);
		idMapping.setTableName(table.getSqlName());
		idMapping.setShardingTableName(shardingTableName);
		getShardingManager().saveIdMappings(idMapping);
		return shardingTableName;
	}

	private Set<String> getTableNameByCriteria(Table table, Criteria<?> criteria) {
		if (!table.isSharding()) {
			return Collections.singleton(table.getSqlName());
		}
		if (criteria == null || CollectionUtils.isEmpty(criteria.getConditions())) {
			throw new RuntimeException("empty conditions");
		}
		//根据分表键的值查询分表
		List<Column> shardingColumns = table.getColumns().stream().filter(Column::isShardingColumn).collect(Collectors.toList());
		Set<String> tables = new TreeSet<>();
		shardingColumns.forEach(r -> {
			criteria.getConditions().stream().filter(k -> k.getPropertyName().equals(r.getJavaName()))
					.forEach(condition -> {
						Object value = condition.getValue();
						if (value == null) {
							logger.warn("condition with sharding column's value is null");
							return;
						}
						if (condition.getOperator() == Operator.equal) {
							String tableNameByValue = getShardingManager().getShardingTableNameByValue(table, String.valueOf(value));
							if (tableNameByValue == null) {
								logger.error("can't get tableName from shardingColumn:" + condition.getPropertyName() + " and shardingValue:" + value);
							} else {
								tables.add(tableNameByValue);
							}
						} else if (condition.getOperator() == Operator.in) {
							if (value instanceof Collection) {
								Collection<?> collection = (Collection<?>) value;
								for (Object object : collection) {
									String tableNameByValue = getShardingManager().getShardingTableNameByValue(table, String.valueOf(object));
									if (tableNameByValue == null) {
										logger.error("can't get tableName from shardingColumn:" + condition.getPropertyName() + " and shardingValue:" + object);
									} else {
										tables.add(tableNameByValue);
									}
								}
							} else if (value.getClass().isArray()) {
								Object[] arrays = (Object[]) value;
								for (Object array : arrays) {
									String tableNameByValue = getShardingManager().getShardingTableNameByValue(table, String.valueOf(array));
									if (tableNameByValue == null) {
										logger.error("can't get tableName from shardingColumn:" + condition.getPropertyName() + " and shardingValue:" + array);
									} else {
										tables.add(tableNameByValue);
									}
								}
							}
						}
					});

		});
		//根据主键查询分表
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			criteria.getConditions().stream().filter(k -> column.getJavaName().equals(k.getPropertyName())).forEach(condition -> {
				Object value = condition.getValue();
				if (value == null) {
					logger.warn("condition with id column's value is null");
					return;
				}
				if (condition.getOperator() == Operator.equal) {
					String tableNameById = getShardingManager().getShardingTableNameById(table, (Serializable) value);
					if (tableNameById != null) {
						tables.add(tableNameById);
					} else {
						logger.error("can't get tableName from id:" + condition.getPropertyName() + " and id is :" + value);
					}
				} else if (condition.getOperator() == Operator.in) {
					if (value instanceof Collection) {
						for (Object object : (Collection<?>) value) {
							String tableNameById = getShardingManager().getShardingTableNameById(table, (Serializable) object);
							if (tableNameById != null) {
								tables.add(tableNameById);
							} else {
								logger.error("can't get tableName from id:" + condition.getPropertyName() + " and id is :" + object);
							}
						}
					} else if (value.getClass().isArray()) {
						Object[] arrays = (Object[]) value;
						for (Object array : arrays) {
							String tableNameById = getShardingManager().getShardingTableNameById(table, (Serializable) array);
							if (tableNameById != null) {
								tables.add(tableNameById);
							} else {
								logger.error("can't get tableName from id:" + condition.getPropertyName() + " and id is :" + array);
							}
						}
					}
				}
			});
		});
		if (tables.isEmpty()) {
			throw new RuntimeException("can't find any table info from table:" + table.getJavaName() + " in criteria:" + criteria.toString());
		}
		return tables;
	}

	private static String getShardingValue(List<String> shardingValues) {
		return shardingValues.stream().reduce((r, s) -> r + "|" + s).orElse(null);
	}

	private static ShardingManager getShardingManager() {
		return ServiceLocator.getService(ShardingManager.class);
	}

	private static Serializable getIdValue(Table table, Object object) {
		return table.getColumns().stream().filter(Column::isPk).findFirst().map(column -> {
			if (object instanceof Map) {
				return (Serializable) ((Map<?, ?>) object).get(column.getJavaName());
			}
			return (Serializable) ReflectionUtils.getFieldValue(object, column.getJavaName());
		}).orElse(null);
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
