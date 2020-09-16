package org.loed.framework.common.query;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/9/16 21:53
 */
 public  interface Copyable<T> {
	/**
	 * 可复制的对象
	 * @return 返回新的复制对象
	 */
	T copy();
}
