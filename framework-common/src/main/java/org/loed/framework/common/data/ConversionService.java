package org.loed.framework.common.data;


import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/8 4:18 下午
 */
public interface ConversionService {
	Logger logger = LoggerFactory.getLogger(ConversionService.class);

	/**
	 * 判断两个不同类型之间的对象是否能相互转换
	 *
	 * @param sourceDataType 源对象类型
	 * @param targetDataType 目标对象类型
	 * @return 是否可以转换
	 */
	default boolean canConvert(DataTypeEnum sourceDataType, DataTypeEnum targetDataType) {
		List<Converter<?>> converters = getConverters(sourceDataType, targetDataType);
		if (CollectionUtils.isEmpty(converters)) {
			return false;
		}
		return converters.stream().anyMatch(cov -> cov.support(sourceDataType));
	}

	/**
	 * 按照源对象值 和 目标对象类型，找到合适的转换器
	 *
	 * @param value          源对象值
	 * @param targetDataType 目标对象类型
	 * @return 转换器
	 */
	default List<Converter<?>> getConverters(Object value, DataTypeEnum targetDataType) {
		DataTypeEnum sourceClass = DataTypeEnum.from(value);
		return getConverters(sourceClass, targetDataType);
	}

	/**
	 * 按照源对象值 和 目标对象类型，找到合适的转换器
	 *
	 * @param sourceDataType 源对象类型
	 * @param targetDataType 目标对象类型
	 * @return 转换器
	 */
	List<Converter<?>> getConverters(DataTypeEnum sourceDataType, DataTypeEnum targetDataType);

	/**
	 * 从源值转换对象到目标类型,如果无法转换，抛出转换异常，如果转换失败，返回null
	 *
	 * @param value       源值
	 * @param targetClass 目标对象
	 * @return 转换后的值
	 * @throws DataConvertException 转换异常
	 */
	@Nullable
	default Object convert(Object value, Class<?> targetClass) throws DataConvertException {
		if (value == null) {
			return null;
		}
		DataTypeEnum sourceDataType = DataTypeEnum.from(value);
		DataTypeEnum targetDataType = DataTypeEnum.of(targetClass);
		//如果类型一致，则无需转换
		if (DataTypeEnum.isSameType(sourceDataType, targetDataType)) {
			return value;
		}
		if (canConvert(sourceDataType, targetDataType)) {
			throw new DataConvertException("can't find converter for value:" + value + " to type:" + targetDataType.name());
		}
		List<Converter<?>> converters = getConverters(sourceDataType, targetDataType);

		List<Converter<?>> sortedConverters = converters.stream().filter(converter -> converter.support(sourceDataType)).sorted(Comparator.comparing(Converter::getPriority))
				.collect(Collectors.toList());
		for (Converter<?> converter : sortedConverters) {
			try {
				return converter.convert(value);
			} catch (DataConvertException convertException) {
				logger.error(convertException.getMessage(), convertException);
			}
		}
		return null;
	}
}
