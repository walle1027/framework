package org.loed.framework.common.data;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/9 2:27 下午
 */
public class DefaultConversionService implements ConversionService {

	@Override
	public List<Converter<?>> getConverters(DataTypeEnum sourceDataType, DataTypeEnum targetDataType) {
		return null;
	}
}
