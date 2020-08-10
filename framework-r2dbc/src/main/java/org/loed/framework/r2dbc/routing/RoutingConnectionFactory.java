package org.loed.framework.r2dbc.routing;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 3:02 下午
 */
public class RoutingConnectionFactory implements ConnectionFactory, InitializingBean {

	private @Nullable
	ConnectionFactory resolvedDefaultConnectionFactory;

	@Override
	public Publisher<? extends Connection> create() {
		return getTargetConnectionFactory()
				.map(ConnectionFactory::create)
				.flatMap(Mono::from);
	}

	@Override
	public ConnectionFactoryMetadata getMetadata() {
		if (resolvedDefaultConnectionFactory != null) {
			return resolvedDefaultConnectionFactory.getMetadata();
		}
		throw new UnsupportedOperationException("No default ConnectionFactory configured to retrieve ConnectionFactoryMetadata");
	}

	protected Mono<ConnectionFactory> getTargetConnectionFactory() {
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}
}
