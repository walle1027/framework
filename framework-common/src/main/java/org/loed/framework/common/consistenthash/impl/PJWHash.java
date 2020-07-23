package org.loed.framework.common.consistenthash.impl;

import org.loed.framework.common.consistenthash.HashFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-30 下午5:07
 */

public class PJWHash implements HashFunction {
	@Override
	public int hash(String str) {
		long BitsInUnsignedInt = (long) (4 * 8);
		long ThreeQuarters = (long) ((BitsInUnsignedInt * 3) / 4);
		long OneEighth = (long) (BitsInUnsignedInt / 8);
		long HighBits = (long) (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
		long hash = 0;
		long test = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = (hash << OneEighth) + str.charAt(i);
			if ((test = hash & HighBits) != 0) {
				hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
			}
		}
		return (int) (hash % base);
	}

	@Override
	public int hash(Object key) {
		return hash(key.toString());
	}
}
