package org.loed.framework.jdbc.datasource.readwriteisolate;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 * @author thomason
 * @date 2019-06-20
 */
public class ReadWriteAop implements MethodInterceptor {
	private Logger logger = LoggerFactory.getLogger(ReadWriteAop.class);

	public ReadWriteAop() {
	}

	protected boolean autoGuessIsRead(String packageName) {
		StringTokenizer tokenizer = new StringTokenizer(packageName, ". ", false);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equalsIgnoreCase("read")) {
				return true;
			}
		}
		return false;
	}

	protected boolean autoGuessIsWrite(String packageName) {
		StringTokenizer tokenizer = new StringTokenizer(packageName, ". ", false);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equalsIgnoreCase("write")) {
				return true;
			}
		}
		return false;
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

	/**
	 * 数据源切换的AOP拦截器
	 *
	 * @param invocation 方法执行切入点
	 * @return 方法执行结果
	 * @throws Throwable 执行异常
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		//default propagation
		ReadWriteIsolatePropagation propagation = ReadWriteIsolatePropagation.required;
		try {
			Method method = invocation.getMethod();
			//代理对象的方法名
			String methodName = method.getName();
			//代理对象的类名
			Class<?> targetClass = invocation.getThis().getClass();
			boolean isRead = false;
			boolean isWrite = false;
			//先读取方法的annotation
			ReadWriteIsolation readWriteIsolation = method.getAnnotation(ReadWriteIsolation.class);
			if (readWriteIsolation != null) {
				propagation = readWriteIsolation.propagation();
				isRead = readWriteIsolation.value().equals(ReadWriteStrategy.read);
				isWrite = readWriteIsolation.value().equals(ReadWriteStrategy.write);
				if (logger.isDebugEnabled()) {
					logger.debug("method:" + methodName + " has annotation: " + ReadWriteIsolation.class.getSimpleName() + " and propagation is :" + propagation.name());
				}
			}
			//如果方法上没有annotation，读取类的annotation
			else {
				ReadWriteIsolation readWriteIsolationClass = targetClass.getAnnotation(ReadWriteIsolation.class);
				if (readWriteIsolationClass != null) {
					propagation = readWriteIsolationClass.propagation();
					isRead = readWriteIsolationClass.value().equals(ReadWriteStrategy.read);
					isWrite = readWriteIsolationClass.value().equals(ReadWriteStrategy.write);
					if (logger.isDebugEnabled()) {
						logger.debug("class:" + targetClass.getName() + " has annotation:" + (isRead ? "read" : "") + (isWrite ? "write" : ""));
					}
				}
				//如果类上面没有annotation 自动根据包名猜测 这个方法可以被重写
//				else {
//					if (logger.isDebugEnabled()) {
//						logger.debug("method:" + methodName + " or class:" + targetClass.getName() + " has no annotation,guess from package:" + targetClass.getName() + " default rwType is " + ReadWriteStrategy.write);
//					}
//					isWrite = true;
//					isRead = false;
//					isRead = autoGuessIsRead(targetClass.getName());
//					isWrite = autoGuessIsWrite(targetClass.getName());
//					if (logger.isDebugEnabled()) {
//						logger.debug("i guess this method is :" + (isRead ? "read" : "") + (isWrite ? "write" : ""));
//					}
//				}
			}
			if (logger.isDebugEnabled()) {
				String signature = getMethodSignature(method,  targetClass);
				if (isWrite) {
					logger.debug("method:" + signature + " marked as :write ");
				}
				if (isRead) {
					logger.debug("method:" + signature + " marked as :read");
				}
			}
			if (isWrite) {
				ReadWriteContext.markAsWrite(propagation);
			} else {
				ReadWriteContext.markAsRead(propagation);
			}
			return invocation.proceed();
		} finally {
			ReadWriteContext.clean(propagation);
		}
	}

	public String getMethodSignature(Method method,Class targetClass) {
		StringBuilder builder = new StringBuilder( targetClass .getName() + "#" + method.getName() + "(");
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes != null && parameterTypes.length > 0) {
			for (Class<?> parameterType : parameterTypes) {
				builder.append(parameterType.getName());
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append(")");
		return builder.toString();
	}
}
