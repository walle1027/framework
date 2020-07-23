/**
 *
 */
package org.loed.framework.common.balancer.impl;

import org.apache.commons.lang3.RandomUtils;
import org.loed.framework.common.balancer.Balanceable;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.balancer.Circle;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author
 */
public abstract class AbstractBalancer<T extends Balanceable> implements Balancer<T> {

	protected volatile Circle<Integer, T> balancerCircle = new Circle<Integer, T>();
	protected Lock lock = new ReentrantLock();
	protected AtomicInteger position;
	protected Collection<String> whiteList = null;
	protected Integer BARRIER = Integer.MAX_VALUE / 2;

	protected AbstractBalancer() {
		position = new AtomicInteger(RandomUtils.nextInt(0, BARRIER));
	}

	@Override
	public T select() {
		if (balancerCircle == null || balancerCircle.size() == 0) {
			return null;
		} else if (balancerCircle.size() == 1) {
			T sp = balancerCircle.firstValue();
			return sp.isAvailable() ? sp : null;
		} else {
			return doSelect();
		}
	}

	protected abstract T doSelect();

	protected T getFromCircle(int code) {
		int size = balancerCircle.size();
		T sp = null;
		if (size > 0) {
			int tmp = code;
			while (size > 0) {
				tmp = balancerCircle.lowerKey(tmp);
				sp = balancerCircle.get(tmp);
				if (sp != null && sp.isAvailable()) {
					break;
				} else {
					sp = null;
				}
				size--;
			}
		}
		return sp;
	}

	@Override
	public void setWhiteList(Collection<String> whiteList) {
		this.whiteList = whiteList;
	}

	@Override
	public void updateProfiles(Collection<T> nodeList) {
		lock.lock();
		try {
			Circle<Integer, T> circle = new Circle<Integer, T>();
			int size = 0;
			for (T node : nodeList) {
				circle.put(size++, node);
			}
			balancerCircle = circle;
		} finally {
			lock.unlock();
		}
	}

}
