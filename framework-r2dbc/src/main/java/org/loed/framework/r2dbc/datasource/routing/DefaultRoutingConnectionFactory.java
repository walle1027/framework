package org.loed.framework.r2dbc.datasource.routing;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.r2dbc.datasource.R2dbcDataSource;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.r2dbc.EmbeddedDatabaseConnection;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
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
public class DefaultRoutingConnectionFactory implements RoutingConnectionFactory, InitializingBean {

	private final R2dbcProvider r2dbcProvider;

	@Autowired(required = false)
	private ObjectProvider<ConnectionFactoryOptionsBuilderCustomizer> customizers;

	private String routingKey;

	private final Map<String, ConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<>();

	public DefaultRoutingConnectionFactory(R2dbcProvider r2dbcProvider) {
		this.r2dbcProvider = r2dbcProvider;
	}

	@Override
	public Publisher<? extends Connection> create() {
		return getTargetConnectionFactory()
				.map(ConnectionFactory::create)
				.flatMap(Mono::from);
	}

	@Override
	public ConnectionFactoryMetadata getMetadata() {
		return () -> "RoutingConnectionFactory";
	}

	protected Mono<ConnectionFactory> getTargetConnectionFactory() {
		return ReactiveSystemContext.getSystemContext().flatMap(context -> {
			String routingValue = context.get(routingKey);
			if (StringUtils.isBlank(routingValue)) {
				return Mono.error(new RuntimeException("can't find routingValue for routingKey " + routingKey));
			}
			return r2dbcProvider.getDataSource(routingKey, routingValue).map(r2dbcDataSource -> {
				String uniqueName = r2dbcDataSource.uniqueName();
				return connectionFactoryMap.computeIfAbsent(uniqueName, key -> {
					return createConnectionFactory(r2dbcDataSource);
				});
			});
		});
	}

	@Override
	public void afterPropertiesSet() {
//		List<R2dbcProperties> r2dbcProperties = propertiesProvider.getAllDataSource();
//		if (CollectionUtils.isEmpty(r2dbcProperties)) {
//			throw new IllegalStateException("No ConnectionFactory properties found");
//		}
//		r2dbcProperties.forEach(properties -> {
//			String uniqueName = properties.determineUniqueName();
//			this.resolvedDefaultConnectionFactory = connectionFactoryMap.computeIfAbsent(uniqueName, key -> {
//				return createConnectionFactory(properties);
//			});
//		});
	}

	private ConnectionFactory createConnectionFactory(R2dbcDataSource r2dbcDataSource) {
		ConnectionFactory connectionFactory = createConnectionFactory(r2dbcDataSource,
				customizers.orderedStream().collect(Collectors.toList()));
		ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
				.maxSize(r2dbcDataSource.getMaxSize()).initialSize(r2dbcDataSource.getInitialSize()).maxIdleTime(r2dbcDataSource.getMaxIdleTime())
				.maxCreateConnectionTime(r2dbcDataSource.getMaxCreateConnectionTime()).maxAcquireTime(r2dbcDataSource.getMaxAcquireTime())
				.maxLifeTime(r2dbcDataSource.getMaxLifeTime())
				.acquireRetry(3)
				.registerJmx(true)
				.name(r2dbcDataSource.uniqueName());

		if (StringUtils.isNotBlank(r2dbcDataSource.getValidationQuery())) {
			builder.validationQuery(r2dbcDataSource.getValidationQuery());
		}
		return new ConnectionPool(builder.build());
	}

	protected ConnectionFactory createConnectionFactory(R2dbcDataSource r2dbcDataSource,
	                                                    List<ConnectionFactoryOptionsBuilderCustomizer> optionsCustomizers) {
		R2dbcProperties r2dbcProperties = new R2dbcProperties();
		r2dbcProperties.setName(r2dbcDataSource.getName());
		r2dbcProperties.setUrl(r2dbcDataSource.getUrl());
		r2dbcProperties.setUsername(r2dbcDataSource.getUsername());
		r2dbcProperties.setPassword(r2dbcDataSource.getPassword());
		r2dbcProperties.setGenerateUniqueName(false);
		return ConnectionFactoryBuilder.of(r2dbcProperties, () -> EmbeddedDatabaseConnection.NONE)
				.configure((options) -> {
					for (ConnectionFactoryOptionsBuilderCustomizer optionsCustomizer : optionsCustomizers) {
						optionsCustomizer.customize(options);
					}
				}).build();
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	@Override
	public R2dbcProvider getR2dbcProvider() {
		return r2dbcProvider;
	}


}
