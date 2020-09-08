package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015/2/21 11:13
 */
@SuppressWarnings("ALL")
public class JPAClassRowMapper<T> implements BiFunction<Row, RowMetadata, T> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Class<T> clazz;
	/*对象的所有属性集合*/
	/*查询结果列与对象属性的映射关系*/
	public static final Map<String, Field> fieldMap = new ConcurrentHashMap<>();

	public JPAClassRowMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	private void setPropertyValue(Object object, String columnName, Object columnValue) {
		//处理map类型的值
		int dataType = DataType.getDataType(object);
		if (DataType.isMapType(dataType)) {
			Map map = (Map) object;
			map.put(columnName, columnValue);
			return;
		}
		//处理有分隔符的情况
		if (columnName.contains(".")) {
			String prefix = columnName.substring(0, columnName.indexOf("."));
			String suffix = columnName.substring(columnName.indexOf(".") + 1);
			//使用缓存 减少反射
			String cacheKey = object.getClass().getName() + "#" + prefix;
			Field field = fieldMap.get(cacheKey);
			if (field == null) {
				field = findSuitableField(object.getClass(), prefix, columnValue);
			}
			if (field == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("can't find field:" + prefix + " for class:" + object.getClass().getName() + ",skip set value");
				}
				return;
			}
			fieldMap.put(cacheKey, field);
			Object fieldValue = getFieldValue(field, object);
			//只能做一对一或多对一的关联关系
			if (fieldValue == null) {
				fieldValue = createObject(field.getType());
				if (fieldValue == null) {
					return;
				}
				setFieldValue(field, object, fieldValue);
			}
			//继续处理
			setPropertyValue(fieldValue, suffix, columnValue);
		} else {
			if (columnValue == null) {
				return;
			}
			String cacheKey = object.getClass().getName() + "#" + columnName;
			Field field = fieldMap.get(cacheKey);
			if (field == null) {
				field = findSuitableField(object.getClass(), columnName, columnValue);
			}
			if (field != null) {
				setFieldValue(field, object, columnValue);
				fieldMap.put(cacheKey, field);
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("can't find field:" + columnName + " for class:" + object.getClass().getName() + ",skip set value");
				}
			}
		}
	}

	private Field findSuitableField(Class clazz, String columnName, Object value) {
		List<Field> fields = ReflectionUtils.getDeclaredFields(clazz);
		StringTokenizer tokenizer = new StringTokenizer(columnName, " -_\t\n\r", false);
		StringBuilder builder = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			builder.append(token.substring(0, 1).toUpperCase());
			builder.append(token.substring(1));
		}
		String fieldName = builder.substring(0, 1).toLowerCase() + builder.substring(1);
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		for (Field field : fields) {
			if (field.getName().equalsIgnoreCase(fieldName) && field.getType().equals(value.getClass())) {
				return field;
			}
		}
		return null;
	}

	private void setFieldValue(Field field, Object object, Object value) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
		try {
			//add type convert
			Class<?> type = field.getType();
			if (value != null) {
				Class<?> valueClass = value.getClass();
				if (Objects.equals(type.getName(), valueClass.getName())) {
					field.set(object, value);
				} else {
					int fieldType = DataType.getDataType(type);
					try {
						Object convertedValue = DataType.toType(value, fieldType);
						if (convertedValue == null) {
							logger.error("convert origin value:" + value + " to target type:" + type + " to null");
						}
						field.set(object, convertedValue);
					} catch (Exception e) {
						logger.error("convert value:" + value + " to type:" + fieldType + " error, caused by:" + e.getMessage(), e);
					}
				}
			} else {
				field.set(object, value);
			}
		} catch (IllegalAccessException e) {
			logger.error("不可能抛出的异常:{}", e);
		}
	}

	private Object getFieldValue(Field field, Object object) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			logger.error("不可能抛出的异常:{}", e);
		}
		return null;
	}

	private Object createObject(Class clazz) {
		if (clazz == null) {
			return null;
		}
		try {
			if (clazz.getName().equals(Map.class.getName())) {
				return new HashMap<>();
			} else if (clazz.getName().equals(Set.class.getName())) {
				return new HashSet<>();
			}
			//TODO create array
			else if (clazz.isArray()) {

			} else if (clazz.getName().equals(List.class.getName())) {
				return new ArrayList<>();
			} else {
				return clazz.newInstance();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public T apply(Row row, RowMetadata rowMetadata) {
		T object = (T) createObject(clazz);
		Collection<String> columnNames = rowMetadata.getColumnNames();

		for (String columnName : columnNames) {
			Object columnValue = row.get(columnName);
			setPropertyValue(object, columnName, columnValue);
		}
		return object;
	}
}
