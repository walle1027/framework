package org.loed.framework.r2dbc.dao;

import org.loed.framework.r2dbc.R2dbcDaoProperties;
import org.loed.framework.r2dbc.listener.spi.*;
import org.loed.framework.r2dbc.query.R2dbcSqlBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.r2dbc.core.DatabaseClient;
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

	private final R2dbcSqlBuilder r2dbcSqlBuilder;


	public R2dbcDaoFactoryBean(Class<? extends R> daoInterface, DatabaseClient databaseClient,R2dbcSqlBuilder r2dbcSqlBuilder) {
		this.daoInterface = daoInterface;
		this.databaseClient = databaseClient;
		this.r2dbcSqlBuilder = r2dbcSqlBuilder;
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
			DefaultR2dbcDao<T, ID> defaultR2DbcDao = new DefaultR2dbcDao<T, ID>(daoInterface, databaseClient);

			defaultR2DbcDao.setR2dbcSqlBuilder(r2dbcSqlBuilder);
			//initial listeners
			defaultR2DbcDao.setPreInsertListeners(lookupBeans(PreInsertListener.class));
			defaultR2DbcDao.setPostInsertListeners(lookupBeans(PostInsertListener.class));
			defaultR2DbcDao.setPreUpdateListeners(lookupBeans(PreUpdateListener.class));
			defaultR2DbcDao.setPostUpdateListeners(lookupBeans(PostUpdateListener.class));
			defaultR2DbcDao.setPreDeleteListeners(lookupBeans(PreDeleteListener.class));

			BindResult<R2dbcDaoProperties> bind = Binder.get(environment).bind(R2dbcDaoProperties.Prefix, R2dbcDaoProperties.class);
			if (bind.isBound()) {
				R2dbcDaoProperties r2dbcDaoProperties = bind.get();
				defaultR2DbcDao.setBatchSize(r2dbcDaoProperties.getBatchSize());
			}
			ProxyFactory result = new ProxyFactory();
			result.setTarget(defaultR2DbcDao);
			result.setInterfaces(daoInterface, R2dbcDao.class, TransactionalProxy.class);

//		if (MethodInvocationValidator.supports(repositoryInterface)) {
//			result.addAdvice(new MethodInvocationValidator());
//		}

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


	protected R2dbcSqlBuilder lookupSqlBuilder() {
		return applicationContext.getBean(R2dbcSqlBuilder.class);
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
