package org.loed.framework.common.balancer.impl;

import org.loed.framework.common.balancer.Balanceable;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 16:31
 */
public class RRBalancer<T extends Balanceable> extends AbstractBalancer<T> {
	@Override
	protected T doSelect() {
		int key = position.getAndIncrement();
		int totalSize = balancerCircle.size();
		int realPos = key % totalSize;
		if (key > BARRIER) {
			position.set(0);
		}
		return getFromCircle(realPos);
	}
}
