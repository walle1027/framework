package org.loed.framework.mybatis.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.loed.framework.common.ThreadPoolExecutor;
import org.loed.framework.mybatis.interceptor.ChainedInterceptor;
import org.loed.framework.mybatis.interceptor.DefaultChainedInterceptor;
import org.loed.framework.mybatis.interceptor.InsertInterceptor;
import org.loed.framework.mybatis.interceptor.SimpleBatchInterceptor;
import org.loed.framework.mybatis.listener.MybatisListenerContainer;
import org.loed.framework.mybatis.listener.impl.DefaultPreInsertListener;
import org.loed.framework.mybatis.listener.impl.DefaultPreUpdateListener;
import org.loed.framework.mybatis.listener.spi.*;
import org.loed.framework.mybatis.sharding.ShardingListByIdsInterceptor;
import org.loed.framework.mybatis.sharding.ShardingManager;
import org.loed.framework.mybatis.sharding.table.impl.DoubleWriteManager;
import org.loed.framework.mybatis.sharding.table.impl.JdbcShardingManager;
import org.loed.framework.mybatis.sharding.table.impl.RedisShardingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
		defaultChainedInterceptor.addInterceptor(ChainedInterceptor.SHARDING_LIST_BY_IDS, new ShardingListByIdsInterceptor());
		defaultChainedInterceptor.addInterceptor(ChainedInterceptor.INSERT_ORDER, new InsertInterceptor());
		return defaultChainedInterceptor;
	}

	@Bean
	public ShardingManager jdbcShardingManager(SqlSessionFactory sqlSessionFactory) {
		JdbcShardingManager jdbcShardingManager = new JdbcShardingManager();
		jdbcShardingManager.setSqlSessionFactory(sqlSessionFactory);
		return jdbcShardingManager;
	}

	@Bean
	public ShardingManager redisShardingManager() {
		return new RedisShardingManager();
	}

	@Bean
	@Primary
	public ShardingManager shardingManager(ThreadPoolExecutor threadPoolExecutor,
	                                       @Qualifier("jdbcShardingManager") ShardingManager jdbcShardingManager
			, @Qualifier("redisShardingManager") ShardingManager redisShardingManager) {
//		DbInspector.addExtPackages(ShardingMapping.class.getPackage().getName());
		DoubleWriteManager manager = new DoubleWriteManager();
		manager.setJdbcShardingManager(jdbcShardingManager);
		manager.setRedisShardingManager(redisShardingManager);
		manager.setPoolExecutor(threadPoolExecutor);
		return manager;
	}

	@PostConstruct
	public void addListeners() {
		MybatisListenerContainer.addPreInsertListeners(preInsertListeners);
		MybatisListenerContainer.addPreUpdateListeners(preUpdateListeners);
		MybatisListenerContainer.addPreDeleteListeners(preDeleteListeners);
		MybatisListenerContainer.addPostInsertListeners(postInsertListeners);
		MybatisListenerContainer.registerPostUpdateListeners(postUpdateListeners);
		MybatisListenerContainer.registerPostDeleteListeners(postDeleteListeners);
	}
}
