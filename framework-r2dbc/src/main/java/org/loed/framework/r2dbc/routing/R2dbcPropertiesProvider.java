package org.loed.framework.r2dbc.routing;

import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 6:38 下午
 */
public interface R2dbcPropertiesProvider {
	/**
	 * 根据路由键和路由值获取R2dbc配置属性
	 *
	 * @param routingKey   路由键
	 * @param routingValue 路由值
	 * @return R2dbcProperties
	 */
	R2dbcProperties getR2dbcProperties(Object routingKey, String routingValue);

	/**
	 * 获取所有配置的R2dbcProperties
	 *
	 * @return 所有的R2dbcProperties
	 */
	List<R2dbcProperties> getAllProperties();
}
