package org.loed.framework.common.balancer;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/6/30 9:21
 */
public interface Balanceable {

	/**
	 * 当前平衡器是否可用
	 *
	 * @return true false
	 */
	boolean isAvailable();

	/**
	 * 获取当前平衡器的权重
	 *
	 * @return 权重
	 */
	int getWeight();
}
