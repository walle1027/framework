package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/8 4:18 下午
 */
public interface ConversionService {
	boolean canConvert(DataTypeEnum sourceClass, DataTypeEnum targetClass);
}
