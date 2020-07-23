package org.loed.framework.r2dbc.dao;

import org.loed.framework.r2dbc.listener.OrderedListener;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.util.Lazy;
import org.springframework.transaction.interceptor.TransactionalProxy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/7 1:33 下午
 */
public class R2dbcDaoFactoryBean<R extends R2dbcDao<T, ID>, T, ID> implements InitializingBean, FactoryBean<R>, BeanClassLoaderAware,
		ApplicationContextAware {

	private ClassLoader classLoader;

	private ApplicationContext applicationContext;

	private final Class<? extends R> daoInterface;

	private Lazy<R> dao;

	private final DatabaseClient databaseClient;


	public R2dbcDaoFactoryBean(Class<? extends R> daoInterface, DatabaseClient databaseClient) {
		this.daoInterface = daoInterface;
		this.databaseClient = databaseClient;
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
			DefaultR2DbcDao<T, ID> defaultR2DbcDao = new DefaultR2DbcDao<T, ID>(daoInterface, databaseClient);
			defaultR2DbcDao.setPreInsertListeners(lookupPreInsertListeners());

			ProxyFactory result = new ProxyFactory();
			result.setTarget(defaultR2DbcDao);
			result.setInterfaces(daoInterface, R2dbcDao.class, TransactionalProxy.class);

//		if (MethodInvocationValidator.supports(repositoryInterface)) {
//			result.addAdvice(new MethodInvocationValidator());
//		}

			result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);

			result.addAdvice(new QueryInterceptor(daoInterface, databaseClient));

			if (DefaultMethodInvokingMethodInterceptor.hasDefaultMethods(daoInterface)) {
				result.addAdvice(new DefaultMethodInvokingMethodInterceptor());
			}

			//

			R proxy = (R) result.getProxy(classLoader);
			return proxy;
		});
	}

	protected List<PreInsertListener> lookupPreInsertListeners() {
		Map<String, PreInsertListener> beans = applicationContext.getBeansOfType(PreInsertListener.class);
		if (beans.size() > 0) {
			return beans.values().stream().sorted(Comparator.comparingInt(OrderedListener::getOrder)).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
