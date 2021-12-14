package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.ConnectionFactory;
import org.loed.framework.r2dbc.autoconfigure.R2dbcProperties;
import org.loed.framework.r2dbc.listener.spi.*;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilderFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.repository.core.support.MethodInvocationValidator;
import org.springframework.data.util.Lazy;
import org.springframework.r2dbc.core.DatabaseClient;
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

	private DatabaseClient databaseClient;

	private Environment environment;

	private ConnectionFactory connectionFactory;

	private R2dbcProperties properties;

	public R2dbcDaoFactoryBean(Class<? extends R> daoInterface) {
		this.daoInterface = daoInterface;
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

			result.addAdvice(new ExecuteInterceptor(databaseClient));

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
		return R2dbcSqlBuilderFactory.getInstance().getSqlBuilder(properties.getDialect(), properties);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setDatabaseClient(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setProperties(R2dbcProperties properties) {
		this.properties = properties;
	}
}
