package org.loed.framework.common.balancer.impl;

import org.apache.commons.lang3.RandomUtils;
import org.loed.framework.common.balancer.Balanceable;
import org.loed.framework.common.balancer.Circle;

import java.util.Collection;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 22:03
 */
public class WeightRandomBalancer<T extends Balanceable> extends AbstractBalancer<T> {
	@Override
	protected T doSelect() {
		int size = balancerCircle.size();
		int random = RandomUtils.nextInt(0, size);
		return balancerCircle.lowerValue(random);
	}

	@Override
	public void updateProfiles(Collection<T> nodeList) {
		lock.lock();
		try {
			Circle<Integer, T> circle = new Circle<Integer, T>();
			int size = 0;
			for (T node : nodeList) {
				int weight = node.getWeight();
				for (int i = 0; i < weight; i++) {
					circle.put(size++, node);
				}
			}
			balancerCircle = circle;
		} finally {
			lock.unlock();
		}
	}
}
