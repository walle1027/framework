package org.loed.framework.common.mapping.impl;

import org.loed.framework.common.mapping.Mapper;
import org.loed.framework.common.mapping.config.MappingConfig;
import org.loed.framework.common.mapping.config.PropertyConfig;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Thomason
 * Date: 11-4-20
 * Time: 下午5:30
 * @version 1.0
 */
@SuppressWarnings({"Duplicates", "unchecked"})
public class MapperImpl implements Mapper {

	protected Logger logger = LoggerFactory.getLogger(MapperImpl.class);
	private Map<String, MappingConfig> configMap;
	private Set<String> defaultIgnoreSet;

	/**
	 * 创建新的映射配置
	 *
	 * @param mappingId     映射配置Id
	 * @param mappingConfig 映射配置
	 */
	@Override
	public void createMappingConfig(String mappingId, MappingConfig mappingConfig) {
		String mappingKey = MappingConfig.buildMappingKey(mappingConfig.getSrcClassType(), mappingConfig.getDestClassType(), mappingId);
		mappingConfig.setMappingKey(mappingKey);
		configMap.put(mappingKey, mappingConfig);
	}

	/**
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 */
	@Override
	public void map(Object origin, Object destination) {
		if (origin == null || destination == null) {
			return;
		}
		map(origin, destination, (String) null);
	}

	/**
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 */
	@Override
	public void map(Object origin, Object destination, String[] ignoreProps) {
		String mappingKey = MappingConfig.buildMappingKey(origin, destination, null);
		MappingConfig mappingConfig = configMap.get(mappingKey);
		if (mappingConfig == null) {
			//如果未配置，调用默认的属性复制器
			if (logger.isDebugEnabled()) {
				logger.debug("srcClass[" + origin.getClass() + "]and destClass[" + destination.getClass() + "] doesn't has a mappingConfig with mappingId:" + mappingKey + ",will use org.apache.commons.beanutils.BeanUtils to copy value");
			}
			copyWithoutMapping(origin, destination, ignoreProps);
		} else {
			copyWithMapping(origin, destination, mappingConfig, ignoreProps);
		}
	}

	/**
	 * fixme 修改枚举型变量的赋值问题
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 * @param mappingId   映射文件Id
	 */
	@Override
	public void map(Object origin, Object destination, String mappingId) {
		String mappingKey = MappingConfig.buildMappingKey(origin, destination, mappingId);
		MappingConfig mappingConfig = configMap.get(mappingKey);
		if (mappingConfig == null) {
			//如果未配置，调用默认的属性复制器
			if (logger.isDebugEnabled()) {
				logger.debug("srcClass[" + origin.getClass() + "]and destClass[" + destination.getClass() + "] doesn't has a mappingConfig with mappingId:" + mappingId + ",will use org.apache.commons.beanutils.BeanUtils to copy value");
			}
			copyWithoutMapping(origin, destination, null);
		} else {
			copyWithMapping(origin, destination, mappingConfig, null);
		}
	}

	/**
	 * 根据映射配置复制对象属性
	 *
	 * @param orig          原始对象
	 * @param dest          目标对象
	 * @param mappingConfig 映射配置
	 */
	private void copyWithMapping(Object orig, Object dest, MappingConfig mappingConfig, String[] ignoreProps) {
		Set<String> ignorePropertySet = new HashSet<>();
		if (ignoreProps != null) {
			Collections.addAll(ignorePropertySet, ignoreProps);
		}
		for (PropertyConfig propertyCfg : mappingConfig.getActualMappingPropertyList()) {
			String destPropertyName = propertyCfg.getDestPropertyName();
			String srcPropertyName = propertyCfg.getSrcPropertyName();
			//忽略不需要复制的属性
			if (ignorePropertySet.contains(destPropertyName)) {
				continue;
			}
			Field origField = ReflectionUtils.getDeclaredField(orig, srcPropertyName);
			Field destField = ReflectionUtils.getDeclaredField(dest, destPropertyName);
			try {
				copyFieldValue(orig, dest, origField, destField, ignoreProps);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param origin      源对象
	 * @param destination 目标对象
	 * @param ignoreProps 忽略属性列表
	 */
	protected void copyWithoutMapping(Object origin, Object destination, String[] ignoreProps) {
		//如果是自定义范围内的类，去掉自动的主键和version复制
		Set<String> ignorePropertySet = new HashSet<>(0);
		if (ignoreProps != null) {
			ignorePropertySet.addAll(Arrays.asList(ignoreProps));
		}

		List<Field> srcFiledList = ReflectionUtils.getDeclaredFields(origin);
		for (Field srcField : srcFiledList) {
			try {
				//如果包含在过滤列表中，跳过
				if (ignorePropertySet.contains(srcField.getName())) {
					continue;
				}
				Field destField = ReflectionUtils.getDeclaredField(destination.getClass(), srcField.getName());
				//复制属性值
				copyFieldValue(origin, destination, srcField, destField, ignoreProps);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("convert object:" + origin + " to object:" + destination + " on field:" + srcField.getName() + "with exception:" + e.getCause());
				}
			}
		}
	}

	/**
	 * 复制对象属性
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 * @param srcField    源对象的属性
	 */
	private void copyFieldValue(Object origin, Object destination, Field srcField, Field destField, String[] ignoreProps) throws IllegalAccessException {
		//目标类如果没有相同属性，跳过
		if (destField == null || srcField == null) {
			return;
		}
		Class<?> srcFieldType = srcField.getType();
		//枚举值
		Class<?> destFieldType = destField.getType();
		Object srcFieldValue = ReflectionUtils.getFieldValue(origin, srcField);
		if (srcFieldValue == null) {
			return;
		}
		int srcDataType = DataType.getDataType(srcFieldType);
		if (DataType.isSimpleType(srcDataType)) {
			int destDataType = DataType.getDataType(destFieldType);
			if (isSameType(srcDataType, destDataType)) {
				ReflectionUtils.setFieldValue(destination, destField, srcFieldValue);
			} else {
				//类型转换
				Object transformedValue = DataType.toType(srcFieldValue, srcDataType, destDataType);
				ReflectionUtils.setFieldValue(destination, destField, transformedValue);
			}
		}
		if (srcFieldType.isEnum()) {
			if (!Modifier.isPublic(destField.getModifiers())
					|| !Modifier.isPublic(destField.getDeclaringClass().getModifiers())) {
				destField.setAccessible(true);
			}
			if (destFieldType.isEnum()) {
				destField.set(destination, srcFieldValue);
			} else {
				int destDataType = DataType.getDataType(destFieldType);
				if (DataType.DT_String == destDataType) {
					destField.set(destination, String.valueOf(srcFieldValue));
				}
			}
			return;
		}
		//TODO 集合暂不处理
		else if (srcDataType == DataType.DT_List) {
			List srcList = (List) srcFieldValue;
			List<Object> destList = new ArrayList<>();
			String typeParameter = ReflectionUtils.getListTypeParameter(destField);
			if (typeParameter == null) {
				logger.debug("can't copy none for property:" + destField.getName());
				return;
			}
			srcList.forEach(c -> {
				try {
					Object dest = Class.forName(typeParameter).newInstance();
					map(c, dest, ignoreProps);
					destList.add(dest);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			ReflectionUtils.setFieldValue(destination, destField, destList);
		} else if (srcDataType == DataType.DT_Set) {
			Set srcSet = (Set) srcFieldValue;
			Set<Object> destSet = new HashSet<>();
			String typeParameter = ReflectionUtils.getSetTypeParameter(destField);
			if (typeParameter == null) {
				logger.debug("can't copy none for property:" + destField.getName());
				return;
			}
			srcSet.forEach(s -> {
				try {
					Object dest = Class.forName(typeParameter).newInstance();
					map(s, dest, ignoreProps);
					destSet.add(dest);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			ReflectionUtils.setFieldValue(destination, destField, destSet);
		} else if (srcDataType == DataType.DT_Array) {
			Object[] srcArray = (Object[]) srcFieldValue;
			Object[] destArray = new Object[srcArray.length];
			String arrayType = ReflectionUtils.getArrayTypeParameter(destField);
			if (arrayType == null) {
				logger.debug("can't copy none for property:" + destField.getName());
				return;
			}
			for (int i = 0; i < srcArray.length; i++) {
				try {
					Object o = srcArray[i];
					Object dest = Class.forName(arrayType).newInstance();
					map(o, dest, ignoreProps);
					destArray[i] = dest;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			ReflectionUtils.setFieldValue(destination, destField, destArray);
		} else if (DataType.isMapType(srcDataType)) {
			//map类型直接copy
			ReflectionUtils.setFieldValue(destination, destField, srcFieldValue);
		}
		//TODO 自定义对象的状况
		else if (srcDataType == DataType.DT_UserDefine) {
			int destDataType = DataType.getDataType(destFieldType);
			//两者都是自定义属性时才会copy
			if (destDataType == DataType.DT_UserDefine) {
				Object destFieldValue = destField.get(destination);
				if (destFieldValue == null) {
					try {
						destFieldValue = destFieldType.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					}
					destField.set(destination, destFieldValue);
				}
				map(srcFieldValue, destFieldValue);
			}
		}
	}


	/**
	 * 判断两个类型是否相同
	 *
	 * @param srcType  源类型
	 * @param destType 目标类型
	 * @return 是否是相同类型
	 */
	protected boolean isSameType(Object srcType, Object destType) {
		return srcType == destType;
	}

	public Map<String, MappingConfig> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, MappingConfig> configMap) {
		this.configMap = configMap;
	}
}
