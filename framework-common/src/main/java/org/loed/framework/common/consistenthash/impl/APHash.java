package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:13
 */

public class APHash implements HashFunction {
	@Override
	public int hash(String str) {
		long hash = 0xAAAAAAAA;
		for (int i = 0; i < str.length(); i++) {
			if ((i & 1) == 0) {
				hash ^= ((hash << 7) ^ str.charAt(i) * (hash >> 3));
			} else {
				hash ^= (~((hash << 11) + str.charAt(i) ^ (hash >> 5)));
			}
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
