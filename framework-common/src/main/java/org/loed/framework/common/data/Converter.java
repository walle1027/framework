package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/8 4:24 下午
 */
public interface Converter<T> {
	T convert(Object origin);
}
