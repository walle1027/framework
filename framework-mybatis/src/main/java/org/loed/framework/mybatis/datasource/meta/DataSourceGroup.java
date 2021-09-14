package org.loed.framework.mybatis.datasource.meta;

import lombok.Data;
import org.loed.framework.common.balancer.Balanceable;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/3/11 2:04 下午
 */
@Data
public class DataSourceGroup implements Balanceable {
	/**
	 * 数据库组名称
	 */
	private String name;
	/**
	 * 自动分配数据源的权重
	 */
	private int weight;
	/**
	 * 主库信息
	 */
	private DataSourceMetaInfo master;
	/**
	 * 从库信息
	 */
	private DataSourceMetaInfo[] slaves;

	@Override
	public boolean isAvailable() {
		return true;
	}
}
