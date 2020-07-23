package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:06
 */

public class JSHash implements HashFunction {
	@Override
	public int hash(String str) {
		long hash = 1315423911;
		for (int i = 0; i < str.length(); i++) {
			hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
