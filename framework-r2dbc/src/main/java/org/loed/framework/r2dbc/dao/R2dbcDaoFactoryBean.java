package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.r2dbc.R2dbcDialect;
import org.loed.framework.r2dbc.autoconfigure.R2dbcProperties;
import org.loed.framework.r2dbc.listener.spi.*;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilderFactory;
import org.loed.framework.r2dbc.routing.R2dbcPropertiesProvider;
import org.loed.framework.r2dbc.routing.RoutingConnectionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.repository.core.support.MethodInvocationValidator;
import org.springframework.data.util.Lazy;
import org.springframework.transaction.interceptor.TransactionalProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 1:33 下午
 */
public class R2dbcDaoFactoryBean<R extends R2dbcDao<T, ID>, T, ID> implements InitializingBean, FactoryBean<R>, BeanClassLoaderAware,
		ApplicationContextAware, EnvironmentAware {

	private ClassLoader classLoader;

	private ApplicationContext applicationContext;

	private final Class<? extends R> daoInterface;

	private Lazy<R> dao;

	private final DatabaseClient databaseClient;

	private Environment environment;

	private final ConnectionFactory connectionFactory;

	@Autowired
	private R2dbcProperties properties;

	public R2dbcDaoFactoryBean(Class<? extends R> daoInterface, DatabaseClient databaseClient, ConnectionFactory connectionFactory) {
		this.daoInterface = daoInterface;
		this.databaseClient = databaseClient;
		this.connectionFactory = connectionFactory;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public R getObject() throws Exception {
		return dao.get();
	}

	@Override
	public Class<?> getObjectType() {
		return daoInterface;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.dao = Lazy.of(() -> {
			DefaultR2dbcDao<T, ID> defaultR2DbcDao = new DefaultR2dbcDao<T, ID>(daoInterface, databaseClient, connectionFactory);

			defaultR2DbcDao.setR2dbcSqlBuilder(createR2dbcSqlBuilder());
			//initial listeners
			defaultR2DbcDao.setPreInsertListeners(lookupBeans(PreInsertListener.class));
			defaultR2DbcDao.setPostInsertListeners(lookupBeans(PostInsertListener.class));
			defaultR2DbcDao.setPreUpdateListeners(lookupBeans(PreUpdateListener.class));
			defaultR2DbcDao.setPostUpdateListeners(lookupBeans(PostUpdateListener.class));
			defaultR2DbcDao.setPreDeleteListeners(lookupBeans(PreDeleteListener.class));

			defaultR2DbcDao.setBatchSize(properties.getBatchSize());

			ProxyFactory result = new ProxyFactory();
			result.setTarget(defaultR2DbcDao);
			result.setInterfaces(daoInterface, R2dbcDao.class, TransactionalProxy.class);

			result.addAdvice(new MethodInvocationValidator());

			result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);

			result.addAdvice(new QueryInterceptor<>(daoInterface, databaseClient));

			if (DefaultMethodInvokingMethodInterceptor.hasDefaultMethods(daoInterface)) {
				result.addAdvice(new DefaultMethodInvokingMethodInterceptor());
			}

			//

			R proxy = (R) result.getProxy(classLoader);
			return proxy;
		});
	}

	protected <R> List<R> lookupBeans(Class<R> clazz) {
		Map<String, R> beans = applicationContext.getBeansOfType(clazz);
		if (beans.size() > 0) {
			return new ArrayList<>(beans.values());
		}
		return null;
	}


	private R2dbcSqlBuilder createR2dbcSqlBuilder() {
		ConnectionFactory connectionFactory = applicationContext.getBean(ConnectionFactory.class);
		org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties r2dbcProperties = null;
		if (connectionFactory instanceof RoutingConnectionFactory) {
			R2dbcPropertiesProvider r2dbcPropertiesProvider = applicationContext.getBean(R2dbcPropertiesProvider.class);
			r2dbcProperties = r2dbcPropertiesProvider.getAllProperties().get(0);
		} else {
			r2dbcProperties = Binder.get(environment).bind("spring.r2dbc", org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties.class)
					.orElse(new org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties());
		}
		R2dbcDialect dialect = R2dbcDialect.autoGuessDialect(r2dbcProperties.getUrl());

		R2dbcSqlBuilder r2dbcSqlBuilder = R2dbcSqlBuilderFactory.getInstance().getSqlBuilder(dialect, properties);
		if (r2dbcSqlBuilder == null) {
			throw new RuntimeException("can't find R2dbcSqlBuilder from properties:" + SerializeUtils.toJson(r2dbcProperties));
		}
		return r2dbcSqlBuilder;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
