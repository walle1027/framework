package org.loed.framework.common.balancer.impl;

import org.loed.framework.common.balancer.Balanceable;
import org.loed.framework.common.balancer.ConditionBalancer;
import org.loed.framework.common.consistenthash.ConsistentHash;
import org.loed.framework.common.consistenthash.ConsistentHashNode;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 22:07
 */
public class ConsistentHashBalancer<T extends Balanceable> implements ConditionBalancer<T, String> {

	protected Lock lock = new ReentrantLock();

	private ConsistentHash<ConsistentHasBalanceAdapter<T>> consistentHash;

	@Override
	public T select() {
		return select("defaultEmptyKey");
	}


	@Override
	public void updateProfiles(Collection<T> nodeList) {
		lock.lock();
		try {
			if (consistentHash == null) {
				consistentHash = new ConsistentHash<>();
			}
			consistentHash.clear();
			for (T node : nodeList) {
				consistentHash.add(new ConsistentHasBalanceAdapter<>(node));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void setWhiteList(Collection<String> whiteList) {

	}

	@Override
	public T select(String condition) {
		ConsistentHasBalanceAdapter<T> consistentHasBalanceAdapter = consistentHash.get(condition);
		if (consistentHasBalanceAdapter == null) {
			return null;
		}
		return consistentHasBalanceAdapter.getDelegate();
	}

	public static class ConsistentHasBalanceAdapter<T extends Balanceable> implements Balanceable, ConsistentHashNode {

		private final T delegate;

		public ConsistentHasBalanceAdapter(T delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean isAvailable() {
			return delegate.isAvailable();
		}

		@Override
		public int getWeight() {
			return delegate.getWeight();
		}

		@Override
		public String hashString() {
			return delegate.toString();
		}

		public T getDelegate() {
			return delegate;
		}
	}
}
