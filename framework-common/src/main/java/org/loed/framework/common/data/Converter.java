package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/8 4:24 下午
 */
public interface Converter<T> {
	/**
	 * 获取转换器的优先级
	 *
	 * @return 优先级
	 */
	int getPriority();

	/**
	 * 是否支持
	 *
	 * @param src 源类型
	 * @return 是否支持
	 */
	boolean support(DataTypeEnum src);

	/**
	 * 从原值转换对象，到目标值，如果转换失败或者无法转换，则抛出转换异常
	 *
	 * @param origin 原值
	 * @return 目标类型
	 * @throws DataConvertException 转换异常
	 */
	T convert(Object origin) throws DataConvertException;
}
