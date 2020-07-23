package org.loed.framework.common.mapping;

import org.loed.framework.common.mapping.config.MappingConfig;
import org.loed.framework.common.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;

/**
 * javabean属性转换接口
 *
 * @author Thomason
 * Date: 11-4-20
 * Time: 下午5:27
 * @version 1.0
 */
public interface Mapper {
	/**
	 * 创建新的映射配置
	 *
	 * @param mappingId     映射配置Id
	 * @param mappingConfig 映射配置
	 */
	void createMappingConfig(String mappingId, MappingConfig mappingConfig);

	/**
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 */
	void map(Object origin, Object destination);

	/**
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 */
	void map(Object origin, Object destination, String[] ignoreProps);

	/**
	 * 从源对象复制属性到目标对象
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 * @param mappingId   映射文件Id
	 */
	void map(Object origin, Object destination, String mappingId);

	/**
	 * 将原对象与目标对象合并
	 *
	 * @param origin      原对象
	 * @param destination 目标对象
	 */
	default void merge(Object origin, Object destination) {
		List<Field> fields = ReflectionUtils.getDeclaredFields(origin);
		if (fields != null) {
			String[] ignoreFields = fields.stream().filter(f -> {
				Object fieldValue = ReflectionUtils.getFieldValue(origin, f);
				if (fieldValue == null) {
					return true;
				}
				return false;
			}).map(Field::getName).toArray(String[]::new);
			map(origin, destination, ignoreFields);
		} else {
			map(origin, destination);
		}
	}

	/**
	 * 复制源对象的所有属性到目标对象中
	 *
	 * @param origin      源对象
	 * @param destination 目标对象
	 */
	default void map(Object origin, Object destination, BiFunction<Object, Object, String[]> function) {
		map(origin, destination, function.apply(origin, destination));
	}
}
