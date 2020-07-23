package org.loed.framework.jdbc;

import org.loed.framework.common.util.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015/2/21 11:13
 */

@SuppressWarnings("ALL")
public class CleverRowMapper<T> implements RowMapper<T> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private boolean isSimpleType;
	private SingleColumnRowMapper singleColumnRowMapper;
	private Class<T> clazz;
	/*对象的所有属性集合*/
	/*查询结果列与对象属性的映射关系*/
	private Map<String, Field> fieldMap;

	public CleverRowMapper(Class<T> clazz) {
		this.clazz = clazz;
		this.isSimpleType = DataType.isSimpleType(DataType.getDataType(clazz));
		fieldMap = new HashMap<>();
		if (isSimpleType) {
			singleColumnRowMapper = new SingleColumnRowMapper();
		}
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		if (isSimpleType) {
			return (T) singleColumnRowMapper.mapRow(rs, rowNum);
		}
		T object = (T) createObject(clazz);
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String columnName = JdbcUtils.lookupColumnName(metaData, i);
			Object columnValue = JdbcUtils.getResultSetValue(rs, i);
			setPropertyValue(object, columnName, columnValue);
		}
		return object;
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
			//TODO 使用缓存 减少反射
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
			field.set(object, value);
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
			if (clazz.getName().equals("java.util.Map")) {
				return new HashMap<>();
			} else if (clazz.getName().equals("java.util.Set")) {
				return new HashSet<>();
			}
			return clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
