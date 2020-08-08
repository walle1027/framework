package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 11:43 上午
 */
@Configuration
@AutoConfigureAfter(ConnectionFactory.class)
public class R2dbcDbInspectorConfiguration implements ApplicationEventPublisherAware, ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//todo
	}
}
