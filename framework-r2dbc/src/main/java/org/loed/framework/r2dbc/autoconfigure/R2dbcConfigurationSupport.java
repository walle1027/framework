package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.listener.impl.DefaultPreInsertListener;
import org.loed.framework.r2dbc.listener.impl.DefaultPreUpdateListener;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreUpdateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/1 3:22 下午
 */
@Configuration
@EnableConfigurationProperties(R2dbcProperties.class)
public class R2dbcConfigurationSupport {
	@Autowired
	private R2dbcProperties properties;

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	public PreInsertListener defaultPreInsertListener() {
		DefaultPreInsertListener preInsertListener = new DefaultPreInsertListener();
		preInsertListener.setOrder(-1);
		return preInsertListener;
	}

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	public PreUpdateListener defaultPreUpdateListener() {
		return new DefaultPreUpdateListener();
	}

	@Bean
	@ConditionalOnBean(ConnectionFactory.class)
	public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}

	/*@Bean(destroyMethod = "dispose")
	ConnectionPool connectionFactory(org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties springProperties, ResourceLoader resourceLoader,
	                                 ObjectProvider<ConnectionFactoryOptionsBuilderCustomizer> customizers) {
		ConnectionFactory connectionFactory = createConnectionFactory(springProperties, resourceLoader.getClassLoader(),
				customizers.orderedStream().collect(Collectors.toList()));
		org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties.Pool springPool = springProperties.getPool();
		R2dbcProperties.Pool poolSupport = properties.getPool();
		ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
				.maxSize(springPool.getMaxSize()).initialSize(springPool.getInitialSize()).maxIdleTime(springPool.getMaxIdleTime())
				.maxAcquireTime(poolSupport.getMaxAcquireTime()).maxCreateConnectionTime(poolSupport.getMaxCreateConnectionTime())
				.acquireRetry(poolSupport.getMaxRetry());
		if (StringUtils.hasText(springPool.getValidationQuery())) {
			builder.validationQuery(springPool.getValidationQuery());
		}
		return new ConnectionPool(builder.build());
	}

	protected ConnectionFactory createConnectionFactory(org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties properties, ClassLoader classLoader,
	                                                    List<ConnectionFactoryOptionsBuilderCustomizer> optionsCustomizers) {
		return ConnectionFactoryBuilder.of(properties, () -> EmbeddedDatabaseConnection.get(classLoader))
				.configure((options) -> {
					for (ConnectionFactoryOptionsBuilderCustomizer optionsCustomizer : optionsCustomizers) {
						optionsCustomizer.customize(options);
					}
				}).build();
	}*/
}
