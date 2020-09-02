package org.loed.framework.common.data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/2 10:27 上午
 */
public interface Converter {
	Integer long2Integer(long value);

	int long2Int(long value);

	Double long2Double(long value);

	double long2double(long value);
}
