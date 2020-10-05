package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.test.listener.impl.DefaultPreInsertListener;
import org.loed.framework.r2dbc.test.listener.impl.DefaultPreUpdateListener;
import org.loed.framework.r2dbc.test.listener.spi.PreInsertListener;
import org.loed.framework.r2dbc.test.listener.spi.PreUpdateListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/1 3:22 下午
 */
@Configuration
public class R2dbcConfigurationSupport {
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
}
