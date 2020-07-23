package org.loed.framework.common.mapping.convertor;

/**
 * @author Thomason
 * Date: 11-4-20
 * Time: 下午5:15
 * @version 1.0
 */
public interface DataConverter {
	/**
	 * 从类型A的值到类型B的值
	 *
	 * @param type      类型A
	 * @param origValue 类型A的值
	 * @return 类型B的值
	 */
	Object convert(Class type, Object origValue);
}
