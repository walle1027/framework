package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:11
 */

public class DJBHash implements HashFunction {
	@Override
	public int hash(String str) {
		long hash = 5381;
		for (int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
