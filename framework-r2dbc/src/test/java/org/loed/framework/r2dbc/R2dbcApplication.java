package org.loed.framework.r2dbc;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.listener.impl.DefaultPreInsertListener;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/30 4:03 PM
 */
@SpringBootApplication
@Import(R2dbcDaoRegister.class)
public class R2dbcApplication  {
	public static void main(String[] args) {
		SpringApplication.run(R2dbcApplication.class, args);
	}

//	@Override
//	@Bean
//	public ConnectionFactory connectionFactory() {
//		ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions.builder();
//		builder.option(ConnectionFactoryOptions.HOST, "127.0.0.1")
//				.option(ConnectionFactoryOptions.PORT, 3306)
//				.option(ConnectionFactoryOptions.USER, "root")
//				.option(ConnectionFactoryOptions.PASSWORD, "123456")
//				.option(ConnectionFactoryOptions.DATABASE, "test")
//
//
//		;
//		return ConnectionFactories.get(builder.build());
//	}

	@Bean
	PreInsertListener preInsertListener(){
		return new DefaultPreInsertListener();
	}

	@Bean
	ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}
}
