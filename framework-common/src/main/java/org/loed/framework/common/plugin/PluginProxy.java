package org.loed.framework.common.plugin;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.plugin.impl.HttpPlugin;
import org.loed.framework.common.plugin.impl.InternalPlugin;
import org.loed.framework.common.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/11/26 13:54
 */
public class PluginProxy {
	//日志记录
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	//插件信息提供器
	private PluginProvider provider;

	/**
	 * 在切入点织入的增强处理，详细配置请参照spring文档
	 *
	 * @param jp 目标增强点的代理对象
	 * @return 目标方法的执行结果
	 * @throws Throwable 异常
	 */
	public Object execute(ProceedingJoinPoint jp) throws Throwable {
		/* 代理目标方法的返回值 */
		Object ret = null;
		/* 代理目标方法的参数 */
		Object target = jp.getTarget();
		//代理对象的类名
		Class<?> targetClass = target.getClass();
		Signature signature = jp.getSignature();
		Object[] args = jp.getArgs();
		Class[] parameterTypes = null;
		if (signature instanceof MethodSignature) {
			parameterTypes = ((MethodSignature) signature).getParameterTypes();
		} else {
			parameterTypes = getParameterTypes(args);
		}
		String methodName = signature.getName();
		String methodSignature = ReflectionUtils.buildMethodSignature(targetClass, methodName, parameterTypes);
		Plugin plugin;
		plugin = provider.getPlugin(SystemContext.getTenantCode(), methodSignature);
		if (plugin != null) {
			return pluginResult(jp, plugin);
		}
		//执行代理对象的方法
		ret = jp.proceed(args);
		return ret;
	}

	private Object pluginResult(ProceedingJoinPoint jp, Plugin plugin) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("tenantCode:" + SystemContext.getTenantCode() + ", method:" + plugin.getSignature() + " has plugin:" + plugin);
		}
		//http请求
		Object[] args = jp.getArgs();
		Class[] argTypes = null;
		Signature signature = jp.getSignature();
		if (signature instanceof MethodSignature) {
			argTypes = ((MethodSignature) signature).getParameterTypes();
		} else {
			argTypes = getParameterTypes(args);
		}
		if (plugin instanceof HttpPlugin) {
			HttpPlugin httpPlugin = (HttpPlugin) plugin;
			String url = httpPlugin.getUrl();
			if (StringUtils.isBlank(url)) {
				throw new RuntimeException("http plugin:" + plugin + "'url is null");
			}
		}
		//内代码的插件实现
		else if (plugin instanceof InternalPlugin) {
			InternalPlugin internal = (InternalPlugin) plugin;
			boolean springBean = internal.isSpringBean();
			Object proxy = null;
			if (springBean) {
				proxy = ServiceLocator.getService(internal.getBeanName());
			} else {
				proxy = Class.forName(internal.getBeanClass()).newInstance();
			}
			if (proxy == null) {
				throw new RuntimeException("internal plugin:" + internal + "'s object is null");
			}
			String methodName = StringUtils.isNotBlank(internal.getMethod()) ? internal.getMethod() : signature.getName();
			Method method = ReflectionUtils.getDeclaredMethod(proxy, methodName, argTypes);
			if (method == null) {
				throw new RuntimeException("method " + methodName + " of bean:" + proxy.getClass() + "done'st exists");
			}
			return method.invoke(proxy, args);
		}
		throw new RuntimeException("unknown plugin protocol :" + plugin);
	}


	private Class[] getParameterTypes(Object[] args) {
		if (args == null) {
			return new Class<?>[0];
		}
		Class<?>[] parameterTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			parameterTypes[i] = args[i].getClass();
		}
		return parameterTypes;
	}

	public void setProvider(PluginProvider provider) {
		this.provider = provider;
	}
}
