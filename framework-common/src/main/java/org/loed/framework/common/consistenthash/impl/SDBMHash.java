package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:11
 */

public class SDBMHash implements HashFunction {
	@Override
	public int hash(String str) {
		long hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
