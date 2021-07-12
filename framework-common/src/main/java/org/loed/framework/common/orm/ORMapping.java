package org.loed.framework.common.orm;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.po.*;
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
	private static final Logger logger = LoggerFactory.getLogger(ORMapping.class);
	private static final ConcurrentMap<Class<?>, Table> orm = new ConcurrentHashMap<>();

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
			Comment comment = clazz.getAnnotation(Comment.class);
			if (comment != null) {
				table.setComment(comment.value());
			}
			//处理索引
			javax.persistence.Index[] indexes = clazzAnnotation.indexes();
			Arrays.stream(indexes).forEach(t -> {
				String indexName = t.name();
				String columnList = t.columnList();
				boolean unique = t.unique();
				org.loed.framework.common.orm.Index index = new org.loed.framework.common.orm.Index(table);
				index.setName(indexName);
				index.setColumnList(columnList);
				index.setUnique(unique);
				table.addIndex(index);
			});
			//处理列
			List<Field> fields = ReflectionUtils.getDeclaredFields(k);
			List<org.loed.framework.common.orm.Column> columns = fields.stream().filter(f -> {
				javax.persistence.Column columnAnno = f.getAnnotation(javax.persistence.Column.class);
				return columnAnno != null;
			}).map(field -> {
				javax.persistence.Column columnAnno = field.getAnnotation(javax.persistence.Column.class);
				org.loed.framework.common.orm.Column column = new org.loed.framework.common.orm.Column(table);
				column.setJavaName(field.getName());
				Class<?> type = field.getType();
				column.setJavaType(type);
				column.setPk(field.getAnnotation(Id.class) != null);
				GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
				if (generatedValue != null) {
					column.setIdGenerationType(generatedValue.strategy());
					column.getTable().setIdGenerationType(generatedValue.strategy());
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
				column.setTenantId(field.getAnnotation(TenantId.class) != null);
				column.setCreateBy(field.getAnnotation(CreateBy.class) != null);
				column.setCreateTime(field.getAnnotation(CreateTime.class) != null);
				column.setDeleted(field.getAnnotation(IsDeleted.class) != null);
				column.setLastModifyBy(field.getAnnotation(LastModifyBy.class) != null);
				column.setLastModifyTime(field.getAnnotation(LastModifyTime.class) != null);
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
			HashSharding hashSharding = k.getAnnotation(HashSharding.class);
			if (hashSharding != null) {
				table.setShardingType(Table.ShardingType.hash);
				Set<String> columnSet = Arrays.stream(hashSharding.columns()).collect(Collectors.toSet());
				List<org.loed.framework.common.orm.Column> shardingColumns = table.getColumns().stream().
						filter(r -> columnSet.contains(r.getSqlName())).collect(Collectors.toList());
				if (shardingColumns.size() > 0) {
					table.setSharding(true);
					table.setShardingCount(hashSharding.count());
					table.setShardingAlias(hashSharding.alias());
					shardingColumns.forEach(s -> s.setShardingColumn(true));
				} else {
					logger.error("error config");
				}
			}
			//处理一对一，一对多，多对一的关系
			fields.stream().filter(f -> f.getAnnotation(OneToOne.class) != null).forEach(field -> {
				OneToOne oneToOne = field.getAnnotation(OneToOne.class);
				Class<?> targetEntity = oneToOne.targetEntity();
				if (targetEntity == void.class) {
					targetEntity = field.getType();
				}
				JoinTable joinTable = new JoinTable(Relation.OneToOne, field.getName(), targetEntity);
				createJoin(table, field, targetEntity, joinTable);
			});
			fields.stream().filter(f -> f.getAnnotation(ManyToOne.class) != null).forEach(field -> {
				ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
				Class<?> targetEntity = manyToOne.targetEntity();
				if (targetEntity == void.class) {
					targetEntity = field.getType();
				}
				JoinTable joinTable = new JoinTable(Relation.ManyToOne, field.getName(), targetEntity);
				createJoin(table, field, targetEntity, joinTable);
			});

			fields.stream().filter(f -> f.getAnnotation(OneToMany.class) != null).forEach(field -> {
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				Class<?> targetEntity = oneToMany.targetEntity();
				if (targetEntity == void.class) {
					//从对象的泛型中获取对象的类型
					targetEntity = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}
				javax.persistence.Table targetTable = (javax.persistence.Table) targetEntity.getAnnotation(javax.persistence.Table.class);
				if (targetTable == null) {
					return;
				}
				JoinTable joinTable = new JoinTable(Relation.OneToMany, field.getName(), targetEntity);
				joinTable.setTargetTableName(targetTable.name());
				String mappedBy = oneToMany.mappedBy();
				if (StringUtils.isNotBlank(mappedBy)) {
					Field targetField = ReflectionUtils.getDeclaredField(targetEntity, mappedBy);
					List<org.loed.framework.common.orm.JoinColumn> joinColumns = getJoinColumns(targetEntity, targetField);
					switchJoinColumn(table, joinTable, joinColumns);
				} else {
					List<Field> declaredFields = ReflectionUtils.getDeclaredFields(targetEntity);
					Field targetFieldByType = declaredFields.stream().filter(df -> df.getType().getCanonicalName().equals(clazz.getCanonicalName()))
							.findFirst().orElse(null);
					if (targetFieldByType == null) {
						return;
					}
					List<org.loed.framework.common.orm.JoinColumn> joinColumns = getJoinColumns(targetEntity, targetFieldByType);
					switchJoinColumn(table, joinTable, joinColumns);
				}
			});
			return table;
		});
	}

	private static void createJoin(Table table, Field field, Class<?> targetEntity, JoinTable joinTable) {
		List<org.loed.framework.common.orm.JoinColumn> joinColumns = getJoinColumns(targetEntity, field);
		if (CollectionUtils.isNotEmpty(joinColumns)) {
			javax.persistence.Table targetTable = (javax.persistence.Table) targetEntity.getAnnotation(javax.persistence.Table.class);
			if (targetTable == null) {
				return;
			}
			joinTable.setTargetTableName(targetTable.name());
			joinTable.setJoinColumns(joinColumns);
			table.addJoin(joinTable);
		}
	}

	private static void switchJoinColumn(Table table, JoinTable joinTable, List<org.loed.framework.common.orm.JoinColumn> joinColumns) {
		if (CollectionUtils.isNotEmpty(joinColumns)) {
			joinColumns.forEach(joinColumn -> {
				String columnName = joinColumn.getName();
				String referencedColumnName = joinColumn.getReferencedColumnName();
				joinColumn.setName(referencedColumnName);
				joinColumn.setReferencedColumnName(columnName);
			});
			joinTable.setJoinColumns(joinColumns);
			table.addJoin(joinTable);
		}
	}

	private static List<org.loed.framework.common.orm.JoinColumn> getJoinColumns(Class clazz, Field field) {
		List<org.loed.framework.common.orm.JoinColumn> columns = new ArrayList<>();
		JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
		if (joinColumns == null) {
			javax.persistence.JoinColumn joinColumn = field.getAnnotation(javax.persistence.JoinColumn.class);
			if (joinColumn == null) {
				return null;
			}
			String joinName = joinColumn.name();
			if (StringUtils.isNotBlank(joinColumn.referencedColumnName())) {
				columns.add(new org.loed.framework.common.orm.JoinColumn(joinName, joinColumn.referencedColumnName()));
			} else {
				columns.add(new org.loed.framework.common.orm.JoinColumn(joinName));
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
			javax.persistence.JoinColumn[] values = joinColumns.value();
			if (values.length > 0) {
				for (javax.persistence.JoinColumn value : values) {
					String joinName = value.name();
					if (StringUtils.isNotBlank(value.referencedColumnName())) {
						columns.add(new org.loed.framework.common.orm.JoinColumn(joinName, value.referencedColumnName()));
					} else {
						columns.add(new org.loed.framework.common.orm.JoinColumn(joinName));
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
				case DataType.DT_byte:
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
				case DataType.DT_boolean:
					return JDBCType.TINYINT;
				case DataType.DT_BigDecimal:
					return JDBCType.DECIMAL;
				default:
					return JDBCType.VARCHAR;
			}
		} else if (dataType == DataType.DT_Enum) {
			return JDBCType.VARCHAR;
		} else {
			return JDBCType.BLOB;
		}
	}
}
