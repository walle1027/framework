package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:01
 */

public class RSHash implements HashFunction {
	@Override
	public int hash(String str) {
		int b = 378551;
		int a = 63689;
		long hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = hash * a + str.charAt(i);
			a = a * b;
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
