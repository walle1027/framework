package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:08
 */

public class ELFHash implements HashFunction {
	@Override
	public int hash(String str) {
		long hash = 0;
		long x = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = (hash << 4) + str.charAt(i);
			if ((x = hash & 0xF0000000L) != 0) {
				hash ^= (x >> 24);
			}
			hash &= ~x;
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
