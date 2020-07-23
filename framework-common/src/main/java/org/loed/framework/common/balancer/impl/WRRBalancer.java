/**
 *
 */
package org.loed.framework.common.balancer.impl;

import org.loed.framework.common.balancer.Balanceable;
import org.loed.framework.common.balancer.Circle;

import java.util.Collection;

/**
 * @author
 */
public class WRRBalancer<T extends Balanceable> extends AbstractBalancer<T> {

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
