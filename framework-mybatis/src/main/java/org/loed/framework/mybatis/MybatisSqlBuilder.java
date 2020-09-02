package org.loed.framework.mybatis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.JdbcType;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.orm.Column;
import org.loed.framework.common.orm.Join;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.query.*;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;
import org.loed.framework.mybatis.sharding.ShardingManager;
import org.loed.framework.mybatis.sharding.table.po.IdMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.JoinType;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/2 下午3:50
 */
@SuppressWarnings({"unused", "Duplicates"})
public class MybatisSqlBuilder {
	public static final String BLANK = " ";
	public static final String INSERT = "org.loed.mybatis.insert";
	public final String TENANT_CODE = "tenant_code";
	private static final String ROOT_TABLE_ALIAS_KEY = "_self";

	private Logger logger = LoggerFactory.getLogger(MybatisSqlBuilder.class);

	public void buildCondition(Map<String, Object> parameterMap, Map<String, String> tableAliasMap, AtomicInteger counter, QueryBuilder sql, Condition condition, Table table) {
		if (condition.hasSubCondition()) {
			if (condition.getJoint() != null) {
				sql.where(condition.getJoint().name() + BLANK + "(");
			} else {
				sql.where("(");
			}
			for (Condition subCondition : condition.getSubConditions()) {
				buildCondition(parameterMap, tableAliasMap, counter, sql, subCondition, table);
			}
			sql.where(")");
		} else {
			buildSingleCondition(parameterMap, tableAliasMap, counter, sql, condition, table);
		}
	}

	private void buildSingleCondition(Map<String, Object> parameterMap, Map<String, String> tableAliasMap, AtomicInteger counter, QueryBuilder sql, Condition condition, Table table) {
		if (!match(condition)) {
			return;
		}

		String propertyName = condition.getPropertyName();
		String joint = (condition.getJoint() == null ? "" : condition.getJoint().name()) + BLANK;
		Object value = condition.getValue();
		Operator operator = condition.getOperator();
		String rawParamName = StringUtils.replace(propertyName, ".", "_") + "Value";
		String uniqueParamName = genUniqueMapKey(rawParamName, parameterMap);
		String alias = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY);
		Column column = resolvePropertyCascade(tableAliasMap, table, alias, counter, sql, condition.getJoinType(), null, propertyName);
		if (column == null) {
			return;
		}
		String columnName = column.getSqlName();
		String jdbcType = column.getSqlTypeName();
		int dataType = DataType.getDataType(column.getJavaType());
		if (condition.isRelativeProperty()) {
			alias = tableAliasMap.get(propertyName.substring(0, propertyName.lastIndexOf(".")));
		}
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
					if ("int[]".equals(simpleName)) {
						int[] values = (int[]) value;
						for (int i = 0; i < values.length; i++) {
							int v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_int, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_int, dataType));
							}
						}
					} else if ("long[]".equals(simpleName)) {
						long[] values = (long[]) value;
						for (int i = 0; i < values.length; i++) {
							long v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_long, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_long, dataType));
							}
						}
					} else if ("char[]".equals(simpleName)) {
						char[] values = (char[]) value;
						for (int i = 0; i < values.length; i++) {
							char v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_char, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_char, dataType));
							}
						}
					} else if ("double[]".equals(simpleName)) {
						double[] values = (double[]) value;
						for (int i = 0; i < values.length; i++) {
							double v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_double, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_double, dataType));
							}
						}
					} else if ("byte[]".equals(simpleName)) {
						byte[] values = (byte[]) value;
						for (int i = 0; i < values.length; i++) {
							byte v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_byte, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_byte, dataType));
							}
						}
					} else if ("short[]".equals(simpleName)) {
						short[] values = (short[]) value;
						for (int i = 0; i < values.length; i++) {
							short v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_short, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_short, dataType));
							}
						}
					} else if ("boolean[]".equals(simpleName)) {
						boolean[] values = (boolean[]) value;
						for (int i = 0; i < values.length; i++) {
							boolean v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_boolean, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_boolean, dataType));
							}
						}
					} else if ("float[]".equals(simpleName)) {
						float[] values = (float[]) value;
						for (int i = 0; i < values.length; i++) {
							float v = values[i];
							if (i == 0) {
								parameterMap.put(betweenKey1, DataType.toType(v, DataType.DT_float, dataType));
							}
							if (i == 1) {
								parameterMap.put(betweenKey2, DataType.toType(v, DataType.DT_float, dataType));
							}
						}
					} else {
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
					if (jdbcType.equals(JdbcType.VARCHAR.toString())) {
						for (Object inValue : collectionValue) {
							//改为参数注入
							builder.append("'");
							builder.append(StringHelper.escapeSql(inValue + ""));
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
					String simpleName = value.getClass().getSimpleName();
					StringBuilder builder = new StringBuilder();
					builder.append(columnNameAlias).append(BLANK).append(condition.getOperator().value());
					builder.append(BLANK);
					builder.append("(");
					if ("int[]".equals(simpleName)) {
						int[] values = (int[]) value;
						for (int v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("long[]".equals(simpleName)) {
						long[] values = (long[]) value;
						for (long v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("char[]".equals(simpleName)) {
						char[] values = (char[]) value;
						for (char v : values) {
							builder.append("'");
							builder.append(v);
							builder.append("'");
							builder.append(",");
						}
					} else if ("double[]".equals(simpleName)) {
						double[] values = (double[]) value;
						for (double v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("byte[]".equals(simpleName)) {
						byte[] values = (byte[]) value;
						for (byte v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("short[]".equals(simpleName)) {
						short[] values = (short[]) value;
						for (short v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("boolean[]".equals(simpleName)) {
						boolean[] values = (boolean[]) value;
						for (boolean v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else if ("float[]".equals(simpleName)) {
						float[] values = (float[]) value;
						for (float v : values) {
							builder.append(v);
							builder.append(",");
						}
					} else {
						Object[] values = (Object[]) value;
						for (Object v : values) {
							builder.append("'");
							builder.append(StringHelper.escapeSql(String.valueOf(v)));
							builder.append("'");
							builder.append(",");
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

	private Column resolvePropertyCascade(Map<String, String> tableAliasMap, Table table, String parentTableAlias, AtomicInteger counter, QueryBuilder sql, JoinType joinType, String path, String propertyName) {
		if (propertyName.contains(".")) {
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

			Class targetEntity = join.getTargetEntity();
			Table targetTable = ORMapping.get(targetEntity);
			assert targetTable != null;
			//done 对分表的支持
			String targetTableName = getTableNameByCriteria(targetTable, null);
			String alias = tableAliasMap.computeIfAbsent(key, (k) -> {
				StringBuilder builder = new StringBuilder();
				String targetAlias = createTableAlias(targetTableName, counter);
				builder.append(targetTableName).append(BLANK).append("as").append(BLANK).append(targetAlias);
				builder.append(BLANK).append("on").append(BLANK);
				join.getJoinColumns().forEach(joinColumn -> {
					if (StringUtils.isNotBlank(parentTableAlias)) {
						builder.append(BLANK).append(parentTableAlias).append(".").append(joinColumn.getName()).append(BLANK);
					} else {
						builder.append(BLANK).append(joinColumn.getName()).append(BLANK);
					}
					builder.append("=").append(BLANK).append(targetAlias).append(".").append(joinColumn.getReferencedColumnName());
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
				targetTable.getColumns().forEach(column -> {
					sql.select(targetAlias + "." + column.getSqlName() + BLANK + "as" + BLANK + "\"" + k + "." + column.getJavaName() + "\"");
				});
				return targetAlias;
			});
			return resolvePropertyCascade(tableAliasMap, targetTable, alias, counter, sql, joinType, key, next);
		} else {
			return table.getColumns().stream().filter(k -> k.getJavaName().equals(propertyName)).findFirst().orElse(null);
		}
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

	public void buildOrder(Map<String, String> tableAliasMap, AtomicInteger counter, QueryBuilder sql, Table table, List<SortProperty> sortProperties) {
		if (CollectionUtils.isNotEmpty(sortProperties)) {
			for (SortProperty sortProperty : sortProperties) {
				String propertyName = sortProperty.getPropertyName();
				if (StringUtils.isBlank(propertyName)) {
					continue;
				}
				String rootAlias = tableAliasMap.get(ROOT_TABLE_ALIAS_KEY);
				//此处的joinType是瞎猜的，不作数
				Column column = resolvePropertyCascade(tableAliasMap, table, rootAlias, counter, sql, JoinType.LEFT, null, propertyName);
				if (column == null) {
					continue;
				}
				String propertyAlias = rootAlias;
				if (propertyName.contains(".")) {
					propertyAlias = tableAliasMap.get(propertyName.substring(0, propertyName.lastIndexOf(".")));
				}
				if (StringUtils.isNotBlank(propertyAlias)) {
					sql.orderBy(propertyAlias + "." + column.getSqlName() + BLANK + sortProperty.getSort().name());
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

	public String insert(Object po) {
		return INSERT;
	}

	public String batchInsert(@Param("list") List<Object> poList, @Param("map") Map<String, Object> map) {
		return BatchType.BatchInsert.name();
	}

	public String update(@Param("po") Object object, @Param("columns") Set<String> columns) {
		Class<?> poClass = object.getClass();
		Table table = ORMapping.get(poClass);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + poClass.getName());
		}
		StringBuilder builder = new StringBuilder();
		builder.append("update");
		builder.append(BLANK);
		List<Serializable> idValue = getPkValues(table, object);
		builder.append(getTableNameByPkValues(table, idValue));
		builder.append(BLANK).append("set").append(BLANK);
		if (table.hasVersionColumn()) {
			Column versionColumn = table.getVersionColumn();
			builder.append(versionColumn.getSqlName()).append(BLANK).append("=").append(BLANK)
					.append(versionColumn.getSqlName()).append(" + 1 ,");
		}
		if (CollectionUtils.isNotEmpty(columns)) {
			table.getColumns().stream().filter(Column::isUpdatable).filter(column -> columns.contains(column.getJavaName())).forEach(column -> {
				builder.append(BLANK).append(column.getSqlName());
				builder.append("=");
				builder.append("#{").append("po.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
				builder.append(",");
			});
		} else {
			table.getColumns().stream().filter(Column::isUpdatable).forEach(column -> {
				builder.append(BLANK).append(column.getSqlName());
				builder.append("=");
				builder.append("#{").append("po.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
				builder.append(",");
			});
		}
		builder.deleteCharAt(builder.length() - 1);
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

	public String updateSelective(@Param("po") Object po, @Param("columns") Set<String> includeColumns) {
		Class<?> poClass = po.getClass();
		Table table = ORMapping.get(poClass);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + poClass.getName());
		}
		StringBuilder builder = new StringBuilder();
		builder.append("update");
		builder.append(BLANK);
		List<Serializable> idValue = getPkValues(table, po);
		builder.append(getTableNameByPkValues(table, idValue));
		builder.append(BLANK);
		builder.append("set").append(BLANK);
		if (table.hasVersionColumn()) {
			Column versionColumn = table.getVersionColumn();
			builder.append(versionColumn.getSqlName()).append(BLANK).append("=").append(BLANK)
					.append(versionColumn.getSqlName()).append(" + 1 ,");
		}
		table.getColumns().stream().filter(Column::isUpdatable).forEach(column -> {
			Object fieldValue = ReflectionUtils.getFieldValue(po, column.getJavaName());
			boolean include = false;
			if (CollectionUtils.isNotEmpty(includeColumns) && includeColumns.contains(column.getJavaName())) {
				include = true;
			} else if (fieldValue != null) {
				include = true;
			}
			if (include) {
				builder.append(BLANK);
				String jdbcType = column.getSqlTypeName();
				builder.append(column.getSqlName()).append("=");
				builder.append("#{po.").append(column.getJavaName()).append(",jdbcType=").append(jdbcType).append("}");
				builder.append(",");
			}
		});
		builder.deleteCharAt(builder.length() - 1);
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

	public String batchUpdateSelective(@Param("clazz") Class<?> clazz
			, @Param("list") List<?> poList, @Param("includeColumns") Set<String> columns) {
		return BatchType.BatchUpdateSelective.name();
	}

	public String batchUpdate(@Param("clazz") Class<?> clazz
			, @Param("list") List<?> poList, @Param("includeColumns") Set<String> columns) {
		return BatchType.BatchUpdate.name();
	}

	public String delete(@Param("map") Map<String, Object> map, @Param("clazz") Class clazz) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		StringBuilder builder = new StringBuilder();
		builder.append("delete from ");
		builder.append(BLANK);
		List<Serializable> pkValues = getPkValues(table, map);
		builder.append(getTableNameByPkValues(table, pkValues));
		builder.append(BLANK);
		builder.append(BLANK).append("where").append(BLANK);
		AtomicInteger pkIndex = new AtomicInteger(0);
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			if (pkIndex.get() > 0) {
				builder.append(BLANK).append("and").append(BLANK);
			}
			builder.append(column.getSqlName()).append(" = #{map.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
			pkIndex.getAndIncrement();
		});
		if (logger.isDebugEnabled()) {
			logger.debug(builder.toString());
		}
		return builder.toString();
//		}
	}

	private static List<Serializable> getPkValues(Table table, Object object) {
		return table.getColumns().stream().filter(Column::isPk).sorted(Comparator.comparing(Column::getJavaName)).map(column -> {
			if (object instanceof Map) {
				return (Serializable) ((Map) object).get(column.getJavaName());
			}
			return (Serializable) ReflectionUtils.getFieldValue(object, column.getJavaName());
		}).collect(Collectors.toList());
	}

	public String updateByCriteria(@Param("clazz") Class clazz, @Param("criteria") Criteria criteria
			, @Param("columnMap") Map<String, Object> columnMap, @Param("map") Map<String, Object> parameterMap) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		QueryBuilder sql = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		//TODO fix this
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		String tableName = getTableNameByCriteria(table, criteria);
		sql.update(tableName);
		columnMap.forEach((key, value) -> {
			Column column = table.getColumns().stream().filter(c -> Objects.equals(c.getJavaName(), key)).findFirst().orElse(null);
			if (column == null) {
				throw new RuntimeException("could not find column for property:" + key + ", in table " + table.getSqlName() + " of class:" + table.getJavaName());
			}
			sql.set(column.getSqlName() + BLANK + "=" + BLANK + "#{columnMap." + key + ",jdbcType=" + column.getSqlTypeName() + "}");
		});
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			for (Condition condition : conditions) {
				buildCondition(parameterMap, tableAliasMap, counter, sql, condition, table);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(sql.toString());
		}
		return sql.toString();
	}

	public String deleteByCriteria(@Param("clazz") Class clazz, @Param("criteria") Criteria criteria, @Param("map") Map<String, Object> parameterMap, @Param("forceDelete") boolean forceDelete) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		QueryBuilder sql = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		sql.delete(getTableNameByCriteria(table, criteria));
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			for (Condition condition : conditions) {
				buildCondition(parameterMap, tableAliasMap, counter, sql, condition, table);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(sql.toString());
		}
		return sql.toString();
	}

	public String get(@Param("clazz") Class clazz, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		StringBuilder builder = new StringBuilder();
		builder.append("select");
		builder.append(BLANK);
		buildSelectItem(null, table, builder);
		builder.append(BLANK);
		builder.append("from");
		builder.append(BLANK);
		List<Serializable> idValue = getPkValues(table, map);
		builder.append(getTableNameByPkValues(table, idValue));
		builder.append(BLANK).append("where").append(BLANK);
		AtomicInteger pkIndex = new AtomicInteger(0);
		table.getColumns().stream().filter(Column::isPk).forEach(column -> {
			if (pkIndex.get() > 0) {
				builder.append(BLANK).append("and").append(BLANK);
			}
			builder.append(column.getSqlName()).append(" = #{map.").append(column.getJavaName()).append(",jdbcType=").append(column.getSqlTypeName()).append("}");
			pkIndex.getAndIncrement();
		});
		if (logger.isDebugEnabled()) {
			logger.debug(builder.toString());
		}
		return builder.toString();
	}

	public String findByCriteria(@Param("clazz") Class clazz, @Param("criteria") Criteria criteria, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}

		QueryBuilder sql = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		String tableName = getTableNameByCriteria(table, criteria);
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(tableName, counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, rootAlias);
		table.getColumns().forEach(column -> {
			sql.select(rootAlias + "." + column.getSqlName() + " as " + "\"" + column.getJavaName() + "\"");
		});
		sql.from(tableName + " as " + rootAlias);
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			for (Condition condition : conditions) {
				buildCondition(map, tableAliasMap, counter, sql, condition, table);
			}
		}
		buildOrder(tableAliasMap, counter, sql, table, criteria.getSortProperties());
		if (logger.isDebugEnabled()) {
			logger.debug(sql.toString());
		}
		return sql.toString();
	}

	public String countByCriteria(@Param("clazz") Class clazz, @Param("criteria") Criteria criteria, @Param("map") Map<String, Object> map) {
		Table table = ORMapping.get(clazz);
		if (table == null) {
			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
		}
		QueryBuilder sql = new QueryBuilder();
		AtomicInteger counter = new AtomicInteger(1);
		Map<String, String> tableAliasMap = new ConcurrentHashMap<>();
		String rootAlias = createTableAlias(table.getSqlName(), counter);
		tableAliasMap.put(ROOT_TABLE_ALIAS_KEY, rootAlias);
		sql.select("count(1)");
		sql.from(table.getSqlName() + BLANK + "as" + BLANK + rootAlias);
		List<Condition> conditions = criteria.getConditions();
		if (CollectionUtils.isNotEmpty(conditions)) {
			for (Condition condition : conditions) {
				buildCondition(map, tableAliasMap, counter, sql, condition, table);
			}
		}
		return sql.toString();
	}

//	public String forceDelete(@Param("id") Serializable id, @Param("clazz") Class<?> clazz) {
//		if (!ReflectionUtils.isInterfaceOf(clazz, Identify.class)) {
//			return null;
//		}
//		Table table = ORMapping.get(clazz);
//		if (table == null) {
//			throw new RuntimeException("not a jpa standard class:" + clazz.getName());
//		}
//		SQL sql = new SQL();
//		sql.DELETE_FROM(table.getSqlName())
//				.WHERE("id = #{id}");
//		return sql.toString();
//	}

	public String sql(@Param("sql") String sql) {
		return sql;
	}

	public void buildSelectItem(Selector selector, Table table, StringBuilder builder) {
		table.getColumns().stream().filter(column -> {
			if (selector != null) {
				return selector.contains(column.getJavaName());
			}
			return true;
		}).forEach(column -> {
			builder.append(BLANK).append(column.getSqlName()).append(BLANK)
					.append("as").append(BLANK)
					.append("\"").append(column.getJavaName()).append("\"");
			builder.append(",");
		});
		builder.deleteCharAt(builder.length() - 1);
	}

	public String shardingGetList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<?> clazz) {
		return BatchType.BatchGetList.name();
	}

	public String shardingGetByIdList(@Param("idList") List<Serializable> idList, @Param("clazz") Class<?> clazz) {
		return BatchType.BatchGetByIdList.name();
	}

	private String getTableNameByPkValues(Table table, List<Serializable> idList) {
		if (!table.isSharding()) {
			return table.getSqlName();
		}
		ShardingManager shardingManager = getShardingManager();
		String idValue = idList.stream().map(String::valueOf).collect(Collectors.joining(","));
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
		List<Serializable> pkValue = getPkValues(table, object);
		String idValue = pkValue.stream().map(String::valueOf).collect(Collectors.joining(","));
		idMapping.setId(idValue);
		idMapping.setShardingKey(shardingColumns.stream().map(Column::getSqlName).collect(Collectors.joining(",")));
		idMapping.setShardingValue(shardingValue);
		idMapping.setTableName(table.getSqlName());
		idMapping.setShardingTableName(shardingTableName);
		getShardingManager().saveIdMappings(idMapping);
		return shardingTableName;
	}

	private String getTableNameByCriteria(Table table, Criteria<?> criteria) {
		if (!table.isSharding()) {
			return table.getSqlName();
		}
		if (criteria == null || CollectionUtils.isEmpty(criteria.getConditions())) {
			throw new RuntimeException("empty conditions");
		}
		//优先检查condition中是否包含分表的值
		List<Column> shardingColumns = table.getColumns().stream().filter(Column::isShardingColumn).collect(Collectors.toList());
		List<String> shardingValues = new ArrayList<>();
		shardingColumns.forEach(r -> {
			criteria.getConditions().stream().filter(k -> k.getPropertyName().equals(r.getJavaName()) && k.getOperator().equals(Operator.equal)).findFirst().ifPresent(condition -> shardingValues.add(String.valueOf(condition.getValue())));
		});
		if (shardingValues.size() != shardingColumns.size()) {
			String idValue = table.getColumns().stream().filter(Column::isPk).sorted(Comparator.comparing(Column::getJavaName)).map(column -> {
				Object value = criteria.getConditions().stream().filter(k -> column.getJavaName().equals(k.getPropertyName()) && k.getOperator().equals(Operator.equal)).findFirst().map(Condition::getValue).orElse(null);
				return String.valueOf(value);
			}).collect(Collectors.joining(","));
			return getShardingManager().getShardingTableNameById(table, idValue);
		} else {
			String shardingValue = getShardingValue(shardingValues);
			return getShardingManager().getShardingTableNameByValue(table, shardingValue);
		}
	}

	private static String getShardingValue(List<String> shardingValues) {
		return shardingValues.stream().reduce((r, s) -> r + "|" + s).orElse(null);
	}

	private static ShardingManager getShardingManager() {
		return ServiceLocator.getService(ShardingManager.class);
	}
}
