package org.loed.framework.common;

import org.loed.framework.common.util.PropertyUtils;
import org.springframework.context.ApplicationContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 服务定位器
 *
 * @author Thomason
 * @version 1.2
 * @since 2009-07-04
 */
@SuppressWarnings({"unchecked"})
public class ServiceLocator {
	//容器类型，可以为Spring或者是EJB
	private static String CONTAINER_SPRING = "spring";
	private static String CONTAINER_EJG = "ejb";

	private static String CONTAINER_TYPE;
	private static ServiceLocator _instance = new ServiceLocator();
	// ejb容器参数
	private static InitialContext initialContext;
	// spring容器
	private static ApplicationContext applicationContext;
	// ejb本地缓存
	private static Map<String, Object> cache;

	private ServiceLocator(String containerType) throws RuntimeException {
		ServiceLocator.CONTAINER_TYPE = containerType;
		try {
			if (CONTAINER_EJG.equals(containerType)) {
				Properties props = PropertyUtils.getProperties("ejb.properties");
				initialContext = new InitialContext(props);
				cache = Collections
						.synchronizedMap(new HashMap<String, Object>());
			} else if (CONTAINER_SPRING.equals(containerType)) {
				//with spring lazy initial
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @throws Exception 服务不存在异常
	 */
	private ServiceLocator() throws RuntimeException {
		this(CONTAINER_SPRING);
	}

	/**
	 * 返回本类的唯一实例
	 *
	 * @return ManagerLocator对象
	 */
	@Deprecated
	public static ServiceLocator getInstance() {
		return _instance;
	}

	/**
	 * 通过类名取得服务对象
	 *
	 * @param clazz 服务接口类
	 * @return the stub
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		Object service = null;
		if (CONTAINER_TYPE.equals(CONTAINER_SPRING)) {
			service = applicationContext.getBean(clazz);
		}
		return (T) service;
	}

	/**
	 * 根据服务的名称(可以是ejb的jndi也可以是spring的bean的名称)取得服务对象
	 *
	 * @param name serviceName
	 * @return the stub
	 */
	public static <T> T getService(String name) {
		return (T) _getService(name);
	}

	/**
	 * 从容器中取出服务对象
	 *
	 * @param name 服务的jndi名称
	 * @return the stub
	 */
	private static <T> T _getService(String name) {
		Object service = null;
		try {
			if (CONTAINER_TYPE.equals(CONTAINER_EJG)) {
				String ejbName = name + "/remote";
				if (cache.containsKey(ejbName)) {
					service = cache.get(ejbName);
				} else {
					service = initialContext.lookup(ejbName);
					cache.put(ejbName, service);
				}
			} else if (CONTAINER_TYPE.equals(CONTAINER_SPRING)) {
				service = applicationContext.getBean(name);
			}
		} catch (NamingException nex) {
			nex.printStackTrace();
		}
		return (T) service;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static void setApplicationContext(ApplicationContext applicationContext) {
		ServiceLocator.applicationContext = applicationContext;
	}
}
