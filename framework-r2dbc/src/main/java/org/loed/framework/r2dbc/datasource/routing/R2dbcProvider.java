package org.loed.framework.r2dbc.datasource.routing;

import org.loed.framework.r2dbc.datasource.MasterSlaveStrategy;
import org.loed.framework.r2dbc.datasource.R2dbcDataSource;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 6:38 下午
 */
public interface R2dbcProvider {
	/**
	 * 根据路由键和路由值获取R2dbc配置属性
	 *
	 * @param routingKey   路由键
	 * @param routingValue 路由值
	 * @return R2dbcDataSource r2dbc 数据源
	 */
	default Mono<R2dbcDataSource> getDataSource(String routingKey, String routingValue) {
		return getDataSource(routingKey, routingValue, MasterSlaveStrategy.master);
	}

	/**
	 * 根据路由键和路由值获取R2dbc配置属性
	 *
	 * @param routingKey   路由键
	 * @param routingValue 路由值
	 * @param strategy     主从策略
	 * @return R2dbcDataSource r2dbc 数据源
	 */
	Mono<R2dbcDataSource> getDataSource(String routingKey, String routingValue, MasterSlaveStrategy strategy);

	/**
	 * 根据路由键和路由值获取R2dbc配置属性
	 *
	 * @param routingKey   路由键
	 * @param routingValue 路由值
	 * @param publisher    主从策略
	 * @return R2dbcDataSource r2dbc 数据源
	 */
	default Mono<R2dbcDataSource> getDataSource(String routingKey, String routingValue, Publisher<MasterSlaveStrategy> publisher) {
		return Mono.from(publisher).flatMap(strategy -> {
			return getDataSource(routingKey, routingValue, strategy);
		});
	}

	/**
	 * 获取所有配置的R2dbcProperties
	 *
	 * @return 所有的R2dbcProperties
	 */
	Flux<R2dbcDataSource> getAllDataSource();
}
