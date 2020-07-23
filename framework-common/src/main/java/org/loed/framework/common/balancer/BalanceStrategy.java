package org.loed.framework.common.balancer;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/4 16:22
 */
public enum BalanceStrategy {
	//轮询
	roundRobin,
	//带权重的轮询
	weightedRoundRobin,
	//随机
	random,
	//带权重的随机
	weightedRandom,
	//一致性hash
	consistentHash
}
