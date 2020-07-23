package org.loed.framework.common.balancer.impl;

import org.apache.commons.lang3.RandomUtils;
import org.loed.framework.common.balancer.Balanceable;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 22:00
 */
public class RandomBalancer<T extends Balanceable> extends AbstractBalancer<T> {
	@Override
	protected T doSelect() {
		int size = balancerCircle.size();
		int random = RandomUtils.nextInt(0, size);
		return balancerCircle.lowerValue(random);
	}
}
