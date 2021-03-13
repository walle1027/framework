package org.loed.framework.r2dbc.datasource.routing;

import io.r2dbc.spi.ConnectionFactory;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/3/11 5:40 下午
 */
public interface RoutingConnectionFactory extends ConnectionFactory {
	/**
	 * 获取r2dbc数据源信息提供
	 *
	 * @return r2dbc数据源信息提供类
	 */
	R2dbcProvider getR2dbcProvider();
}
