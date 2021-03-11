package org.loed.framework.mybatis.datasource.meta;

import org.loed.framework.common.balancer.BalanceStrategy;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.balancer.BalancerFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/8/21 18:38
 */
public class BalancedDataSource implements Serializable {
	/**
	 * 负载均衡策略
	 */
	private final BalanceStrategy strategy;
	/**
	 * 主库
	 */
	private DataSourceMetaInfo master;
	/**
	 * 备库列表
	 */
	private final Balancer<DataSourceMetaInfo> slaves;

	public BalancedDataSource(BalanceStrategy strategy) {
		this.strategy = strategy;
		this.slaves = BalancerFactory.getBalancer(this.strategy);
	}

	public void update(DataSourceGroup dataSourceGroup) {
		this.master = dataSourceGroup.getMaster();
		DataSourceMetaInfo[] slaves = dataSourceGroup.getSlaves();
		if (slaves != null) {
			this.slaves.updateProfiles(Arrays.asList(slaves));
		}
	}

	public DataSourceMetaInfo getMaster() {
		return master;
	}

	public DataSourceMetaInfo getSlave() {
		return this.slaves.select();
	}

	public BalanceStrategy getStrategy() {
		return strategy;
	}

	public Balancer<DataSourceMetaInfo> getSlaves() {
		return slaves;
	}
}
