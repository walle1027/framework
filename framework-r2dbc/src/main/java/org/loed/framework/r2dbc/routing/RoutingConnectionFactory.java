package org.loed.framework.r2dbc.routing;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.r2dbc.EmbeddedDatabaseConnection;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 3:02 下午
 */
public class RoutingConnectionFactory implements ConnectionFactory, InitializingBean {

	private @Nullable
	ConnectionFactory resolvedDefaultConnectionFactory;

	private final R2dbcPropertiesProvider propertiesProvider;

	@Autowired(required = false)
	private ObjectProvider<ConnectionFactoryOptionsBuilderCustomizer> customizers;

	private String routingKey;

	private final Map<String, ConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<>();

	public RoutingConnectionFactory(R2dbcPropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

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
		return ReactiveSystemContext.getSystemContext().map(context -> {
			String routingValue = context.get(routingKey);
			if (StringUtils.isBlank(routingValue)) {
				throw new RuntimeException("can't find routingValue for routingKey " + routingKey);
			}
			R2dbcProperties r2dbcProperties = propertiesProvider.getR2dbcProperties(routingKey, routingValue);
			if (r2dbcProperties == null) {
				throw new RuntimeException("can't find routing connectionFactory for routingKey " + routingKey + " with routingValue " + routingValue);
			}
			String uniqueName = r2dbcProperties.determineUniqueName();
			return connectionFactoryMap.computeIfAbsent(uniqueName, key -> {
				return createConnectionFactory(r2dbcProperties);
			});
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<R2dbcProperties> r2dbcProperties = propertiesProvider.getAllProperties();
		if (CollectionUtils.isEmpty(r2dbcProperties)) {
			throw new IllegalStateException("No ConnectionFactory properties found");
		}
		r2dbcProperties.forEach(properties -> {
			String uniqueName = properties.determineUniqueName();
			this.resolvedDefaultConnectionFactory = connectionFactoryMap.computeIfAbsent(uniqueName, key -> {
				return createConnectionFactory(properties);
			});
		});
	}

	private ConnectionFactory createConnectionFactory(R2dbcProperties properties) {
		ConnectionFactory connectionFactory = createConnectionFactory(properties,
				customizers.orderedStream().collect(Collectors.toList()));
		R2dbcProperties.Pool pool = properties.getPool();
		ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
				.maxSize(pool.getMaxSize()).initialSize(pool.getInitialSize()).maxIdleTime(pool.getMaxIdleTime());
		if (StringUtils.isNotBlank(pool.getValidationQuery())) {
			builder.validationQuery(pool.getValidationQuery());
		}
		return new ConnectionPool(builder.build());
	}

	protected ConnectionFactory createConnectionFactory(R2dbcProperties properties,
	                                                    List<ConnectionFactoryOptionsBuilderCustomizer> optionsCustomizers) {
		return ConnectionFactoryBuilder.of(properties, () -> EmbeddedDatabaseConnection.NONE)
				.configure((options) -> {
					for (ConnectionFactoryOptionsBuilderCustomizer optionsCustomizer : optionsCustomizers) {
						optionsCustomizer.customize(options);
					}
				}).build();
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	@Nullable
	public ConnectionFactory getDefaultConnectionFactory() {
		return resolvedDefaultConnectionFactory;
	}

	public R2dbcPropertiesProvider getPropertiesProvider() {
		return propertiesProvider;
	}
}
