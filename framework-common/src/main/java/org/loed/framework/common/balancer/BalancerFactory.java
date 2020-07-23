package org.loed.framework.common.balancer;

import org.loed.framework.common.balancer.impl.*;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 16:05
 */
public class BalancerFactory {
	public static <T extends Balanceable> Balancer<T> getBalancer(BalanceStrategy balanceStrategy) {
		if (balanceStrategy == null) {
			throw new IllegalArgumentException("Balancer name must not null");
		}
		switch (balanceStrategy) {
			case consistentHash:
				return (Balancer<T>) new ConsistentHashBalancer();
			case random:
				return new RandomBalancer<T>();
			case roundRobin:
				return new RRBalancer<T>();
			case weightedRandom:
				return new WeightRandomBalancer<T>();
			case weightedRoundRobin:
				return new WRRBalancer<T>();
			default:
				return new RRBalancer<T>();
		}
	}
}
