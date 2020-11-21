package org.loed.framework.mybatis.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.loed.framework.mybatis.interceptor.ChainedInterceptor;
import org.loed.framework.mybatis.interceptor.DefaultChainedInterceptor;
import org.loed.framework.mybatis.interceptor.impl.InsertInterceptor;
import org.loed.framework.mybatis.interceptor.impl.SimpleBatchInterceptor;
import org.loed.framework.mybatis.listener.MybatisListenerContainer;
import org.loed.framework.mybatis.listener.impl.DefaultPreInsertListener;
import org.loed.framework.mybatis.listener.impl.DefaultPreUpdateListener;
import org.loed.framework.mybatis.listener.spi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 下午12:41
 */
@Configuration
@ConditionalOnClass(SqlSessionFactory.class)
public class MybatisAutoConfiguration {
	@Autowired(required = false)
	private List<PreInsertListener> preInsertListeners;

	@Autowired(required = false)
	private List<PreUpdateListener> preUpdateListeners;

	@Autowired(required = false)
	private List<PreDeleteListener> preDeleteListeners;

	@Autowired(required = false)
	private List<PostUpdateListener> postUpdateListeners;

	@Autowired(required = false)
	private List<PostInsertListener> postInsertListeners;

	@Autowired(required = false)
	private List<PostDeleteListener> postDeleteListeners;

	@Bean
	public PreInsertListener defaultPreInsertListener() {
		return new DefaultPreInsertListener();
	}

	@Bean
	public PreUpdateListener defaultPreUpdateListener() {
		return new DefaultPreUpdateListener();
	}

	@Bean
	public ChainedInterceptor chainedInterceptor() {
		DefaultChainedInterceptor defaultChainedInterceptor = new DefaultChainedInterceptor();
		defaultChainedInterceptor.addInterceptor(ChainedInterceptor.SIMPLE_BATCH_ORDER, new SimpleBatchInterceptor());
//		defaultChainedInterceptor.addInterceptor(ChainedInterceptor.SHARDING_LIST_BY_IDS, new ShardingListByIdsInterceptor());
		defaultChainedInterceptor.addInterceptor(ChainedInterceptor.INSERT_ORDER, new InsertInterceptor());
		return defaultChainedInterceptor;
	}

	@PostConstruct
	public void addListeners() {
		MybatisListenerContainer.registerPreInsertListeners(preInsertListeners);
		MybatisListenerContainer.registerPreUpdateListeners(preUpdateListeners);
		MybatisListenerContainer.registerPreDeleteListeners(preDeleteListeners);
		MybatisListenerContainer.registerPostInsertListeners(postInsertListeners);
		MybatisListenerContainer.registerPostUpdateListeners(postUpdateListeners);
		MybatisListenerContainer.registerPostDeleteListeners(postDeleteListeners);
	}
}
