package org.loed.framework.common.consistenthash;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-29 下午6:21
 */

public interface HashFunction {
	long base = Integer.MAX_VALUE;

	int hash(String str);

	int hash(Object key);
}
