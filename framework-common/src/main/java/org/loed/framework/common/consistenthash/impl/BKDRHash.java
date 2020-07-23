package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:09
 */

public class BKDRHash implements HashFunction {
	@Override
	public int hash(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
