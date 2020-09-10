package org.loed.framework.common.data.converter;

import org.loed.framework.common.data.Converter;
import org.loed.framework.common.data.DataTypeEnum;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/9 1:48 下午
 */
public abstract class SimpleTypeConverter<T> implements Converter<T> {
	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean support(DataTypeEnum src) {
		return DataTypeEnum.isSimpleType(src);
	}
}
