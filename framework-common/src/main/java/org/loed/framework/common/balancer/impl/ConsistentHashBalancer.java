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
public class ConsistentHashBalancer implements ConditionBalancer<Balanceable, String> {

	protected Lock lock = new ReentrantLock();

	private ConsistentHash consistentHash;

	@Override
	public Balanceable select() {
		return select("defaultEmptyKey");
	}


	@Override
	public void updateProfiles(Collection<Balanceable> nodeList) {
		try {
			lock.lock();
			if (consistentHash == null) {
				consistentHash = new ConsistentHash();
			}
			consistentHash.clear();
			for (Balanceable balanceable : nodeList) {
				consistentHash.add(new ConstiantHasBalanceAdapter(balanceable));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void setWhiteList(Collection whiteList) {

	}

	@Override
	public Balanceable select(String condition) {
		ConstiantHasBalanceAdapter constiantHasBalanceAdapter = (ConstiantHasBalanceAdapter) consistentHash.get(condition);
		if (constiantHasBalanceAdapter == null) {
			return null;
		}
		return constiantHasBalanceAdapter.getBalanceable();
	}

	public static class ConstiantHasBalanceAdapter implements Balanceable, ConsistentHashNode {

		private Balanceable balanceable;

		public ConstiantHasBalanceAdapter(Balanceable balanceable) {
			this.balanceable = balanceable;
		}

		@Override
		public boolean isAvailable() {
			return balanceable.isAvailable();
		}

		@Override
		public int getWeight() {
			return balanceable.getWeight();
		}

		@Override
		public String hashString() {
			return balanceable.toString();
		}

		public Balanceable getBalanceable() {
			return balanceable;
		}

		public void setBalanceable(Balanceable balanceable) {
			this.balanceable = balanceable;
		}
	}
}
