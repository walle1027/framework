package org.loed.framework.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.database.Join;
import org.loed.framework.common.database.Relation;
import org.loed.framework.common.database.Sharding;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.util.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/9 下午3:38
 */
public class ORMapping {
	private static Logger logger = LoggerFactory.getLogger(ORMapping.class);
	private static ConcurrentMap<Class<?>, Table> orm = new ConcurrentHashMap<>();

	public static Table get(Class<?> clazz) {
		javax.persistence.Table clazzAnnotation = clazz.getAnnotation(javax.persistence.Table.class);
		if (clazzAnnotation == null) {
			throw new RuntimeException("target class:" + clazz.getName() + " has'nt a javax.persistence.Table annotation");
		}
		return orm.computeIfAbsent(clazz, k -> {
			Table table = new Table();
			table.setSimpleJavaName(k.getSimpleName());
			table.setJavaName(k.getName());
			String name = clazzAnnotation.name();
			if (StringUtils.isBlank(name)) {
				name = k.getSimpleName();
			}
			table.setSqlName(name);
			table.setCatalog(clazzAnnotation.catalog());
			table.setSchema(clazzAnnotation.schema());
			//处理索引
			Index[] indexes = clazzAnnotation.indexes();
			Arrays.stream(indexes).forEach(t -> {
				String indexName = t.name();
				String columnList = t.columnList();
				boolean unique = t.unique();
				org.loed.framework.common.database.Index index = new org.loed.framework.common.database.Index(table);
				index.setName(indexName);
				index.setColumnList(columnList);
				index.setUnique(unique);
				table.addIndex(index);
			});
			//处理列
			List<Field> fields = ReflectionUtils.getDeclaredFields(k);
			List<org.loed.framework.common.database.Column> columns = fields.stream().filter(f -> {
				Column columnAnno = f.getAnnotation(Column.class);
				return columnAnno != null;
			}).map(field -> {
				Column columnAnno = field.getAnnotation(Column.class);
				org.loed.framework.common.database.Column column = new org.loed.framework.common.database.Column(table);
				column.setJavaName(field.getName());
				Class<?> type = field.getType();
				column.setJavaType(type);
				column.setPk(field.getAnnotation(Id.class) != null);
				GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
				if (generatedValue != null) {
					column.setIdGenerationType(generatedValue.strategy());
				}
				//TODO 计算是否fk
				column.setLength(columnAnno.length());
				column.setScale(columnAnno.scale());
				column.setPrecision(columnAnno.precision());
				column.setNullable(columnAnno.nullable());
				column.setUnique(columnAnno.unique());
				column.setUpdatable(columnAnno.updatable());
				column.setInsertable(columnAnno.insertable());
				column.setVersioned(field.getAnnotation(Version.class) != null);
				//自动设置columnDef
				JDBCType jdbcType = autoGuessJdbcType(field);
				column.setSqlType(jdbcType.getVendorTypeNumber());
				column.setSqlTypeName(jdbcType.getName());

				String columnName = columnAnno.name();
				if (StringUtils.isBlank(columnName)) {
					columnName = StringHelper.camelToUnderline(field.getName());
				}
				column.setSqlName(columnName);
				String definition = columnAnno.columnDefinition();
				if (StringUtils.isNotBlank(definition)) {
					column.setColumnDefinition(definition);
				}
				if (table.getIndices() != null) {
					column.setIndexed(table.getIndices().stream().anyMatch(index -> index.getColumnList().contains(column.getSqlName())));
				}
				//版本列
				if (field.getAnnotation(Version.class) != null) {
					table.setVersionColumn(column);
				}
				return column;
			}).collect(Collectors.toList());
			//先加主键列
			columns.forEach(column -> {
				if (column.isPk()) {
					table.addColumn(column);
				}
			});
			//再加其他列
			columns.forEach(column -> {
				if (!column.isPk()) {
					table.addColumn(column);
				}
			});
			//检查是否分表
			Sharding sharding = k.getAnnotation(Sharding.class);
			if (sharding != null) {
				Set<String> columnSet = Arrays.stream(sharding.columns()).collect(Collectors.toSet());
				List<org.loed.framework.common.database.Column> shardingColumns = table.getColumns().stream().
						filter(r -> columnSet.contains(r.getSqlName())).collect(Collectors.toList());
				if (shardingColumns != null && shardingColumns.size() > 0) {
					table.setSharding(true);
					table.setShardingCount(sharding.count());
					table.setShardingAlias(sharding.alias());
					shardingColumns.forEach(s -> s.setShardingColumn(true));
				} else {
					logger.error("error config");
				}
			}
			//处理一对一，一对多，多对一的关系
			fields.stream().filter(f -> f.getAnnotation(OneToOne.class) != null).forEach(field -> {
				OneToOne oneToOne = field.getAnnotation(OneToOne.class);
				Class targetEntity = oneToOne.targetEntity();
				if (targetEntity == void.class) {
					targetEntity = field.getType();
				}
				Join join = new Join(Relation.OneToOne, field.getName(), targetEntity);
				createJoin(table, field, targetEntity, join);
			});
			fields.stream().filter(f -> f.getAnnotation(ManyToOne.class) != null).forEach(field -> {
				ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
				Class targetEntity = manyToOne.targetEntity();
				if (targetEntity == void.class) {
					targetEntity = field.getType();
				}
				Join join = new Join(Relation.ManyToOne, field.getName(), targetEntity);
				createJoin(table, field, targetEntity, join);
			});

			fields.stream().filter(f -> f.getAnnotation(OneToMany.class) != null).forEach(field -> {
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				Class targetEntity = oneToMany.targetEntity();
				if (targetEntity == void.class) {
					//从对象的泛型中获取对象的类型
					targetEntity = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}
				javax.persistence.Table targetTable = (javax.persistence.Table) targetEntity.getAnnotation(javax.persistence.Table.class);
				if (targetTable == null) {
					return;
				}
				Join join = new Join(Relation.OneToMany, field.getName(), targetEntity);
				join.setTargetTableName(targetTable.name());
				String mappedBy = oneToMany.mappedBy();
				if (StringUtils.isNotBlank(mappedBy)) {
					Field targetField = ReflectionUtils.getDeclaredField(targetEntity, mappedBy);
					List<org.loed.framework.common.database.JoinColumn> joinColumns = getJoinColumns(targetEntity, targetField);
					switchJoinColumn(table, join, joinColumns);
				} else {
					List<Field> declaredFields = ReflectionUtils.getDeclaredFields(targetEntity);
					Field targetFieldByType = declaredFields.stream().filter(df -> df.getType().getCanonicalName().equals(clazz.getCanonicalName()))
							.findFirst().orElse(null);
					if (targetFieldByType == null) {
						return;
					}
					List<org.loed.framework.common.database.JoinColumn> joinColumns = getJoinColumns(targetEntity, targetFieldByType);
					switchJoinColumn(table, join, joinColumns);
				}
			});
			return table;
		});
	}

	private static void createJoin(Table table, Field field, Class targetEntity, Join join) {
		List<org.loed.framework.common.database.JoinColumn> joinColumns = getJoinColumns(targetEntity, field);
		if (CollectionUtils.isNotEmpty(joinColumns)) {
			javax.persistence.Table targetTable = (javax.persistence.Table) targetEntity.getAnnotation(javax.persistence.Table.class);
			if (targetTable == null) {
				return;
			}
			join.setTargetTableName(targetTable.name());
			join.setJoinColumns(joinColumns);
			table.addJoin(join);
		}
	}

	private static void switchJoinColumn(Table table, Join join, List<org.loed.framework.common.database.JoinColumn> joinColumns) {
		if (CollectionUtils.isNotEmpty(joinColumns)) {
			joinColumns.forEach(joinColumn -> {
				String columnName = joinColumn.getName();
				String referencedColumnName = joinColumn.getReferencedColumnName();
				joinColumn.setName(referencedColumnName);
				joinColumn.setReferencedColumnName(columnName);
			});
			join.setJoinColumns(joinColumns);
			table.addJoin(join);
		}
	}

	private static List<org.loed.framework.common.database.JoinColumn> getJoinColumns(Class clazz, Field field) {
		List<org.loed.framework.common.database.JoinColumn> columns = new ArrayList<>();
		JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
		if (joinColumns == null) {
			JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
			if (joinColumn == null) {
				return null;
			}
			String joinName = joinColumn.name();
			if (StringUtils.isNotBlank(joinColumn.referencedColumnName())) {
				columns.add(new org.loed.framework.common.database.JoinColumn(joinName, joinColumn.referencedColumnName()));
			} else {
				columns.add(new org.loed.framework.common.database.JoinColumn(joinName));
			}
			//verify the joinColumn,this may not need
			/*fields.stream().filter(f -> f.getAnnotation(Column.class) != null).forEach(f->{
				Column column = f.getAnnotation(Column.class);
				String columnName = column.name();
				if(StringUtils.isBlank(columnName)){
					columnName = StringHelper.camelToUnderline(f.getName());
				}
				if(columnName.equals(joinName)){
					org.loed.framework.common.database.JoinColumn joinColumn1 = new org.loed.framework.common.database.JoinColumn(joinName);
					if(StringUtils.isNotBlank(joinColumn.referencedColumnName())){
						joinColumn1.setReferencedColumnName(joinColumn.referencedColumnName());
					}
					columns.add(joinColumn1);
				}
			});*/
		} else {
			JoinColumn[] values = joinColumns.value();
			if (values.length > 0) {
				for (JoinColumn value : values) {
					String joinName = value.name();
					if (StringUtils.isNotBlank(value.referencedColumnName())) {
						columns.add(new org.loed.framework.common.database.JoinColumn(joinName, value.referencedColumnName()));
					} else {
						columns.add(new org.loed.framework.common.database.JoinColumn(joinName));
					}
					/*fields.stream().filter(f -> f.getAnnotation(Column.class) != null).forEach(f->{
						Column column = f.getAnnotation(Column.class);
						String columnName = column.name();
						if(StringUtils.isBlank(columnName)){
							columnName = StringHelper.camelToUnderline(f.getName());
						}
						if(columnName.equals(joinName)){
							columns.add(columnName);
						}
					});*/
				}
			}
		}
		return columns;
	}

	public static JDBCType autoGuessJdbcType(Field field) {
		Class<?> fieldType = field.getType();
		int dataType = DataType.getDataType(fieldType);
		if (DataType.isSimpleType(dataType)) {
			switch (dataType) {
				case DataType.DT_Byte:
				case DataType.DT_short:
					return JDBCType.TINYINT;
				case DataType.DT_int:
				case DataType.DT_Integer:
					return JDBCType.INTEGER;
				case DataType.DT_Long:
				case DataType.DT_long:
				case DataType.DT_BigInteger:
					return JDBCType.BIGINT;
				case DataType.DT_Double:
				case DataType.DT_double:
					return JDBCType.DECIMAL;
				case DataType.DT_Float:
				case DataType.DT_float:
					return JDBCType.DECIMAL;
				case DataType.DT_Character:
				case DataType.DT_char:
				case DataType.DT_String:
					return JDBCType.VARCHAR;
				case DataType.DT_Date:
				case DataType.DT_DateTime:
				case DataType.DT_Time:
					Temporal temporal = field.getAnnotation(Temporal.class);
					if (temporal != null) {
						switch (temporal.value()) {
							case DATE:
								return JDBCType.DATE;
							case TIME:
								return JDBCType.TIME;
							case TIMESTAMP:
								return JDBCType.TIMESTAMP;
						}
					} else {
						return JDBCType.TIMESTAMP;
					}

				case DataType.DT_Boolean:
					return JDBCType.TINYINT;
				case DataType.DT_BigDecimal:
					return JDBCType.DECIMAL;
				default:
					return JDBCType.VARCHAR;
			}
		} else if (dataType == DataType.DT_ENUM) {
			return JDBCType.VARCHAR;
		} else {
			return JDBCType.BLOB;
		}
	}
}
