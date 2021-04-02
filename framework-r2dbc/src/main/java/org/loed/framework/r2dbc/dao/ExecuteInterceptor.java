package org.loed.framework.r2dbc.dao;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.r2dbc.query.Execute;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/15 4:48 下午
 */
@SuppressWarnings("DuplicatedCode")
public class ExecuteInterceptor implements MethodInterceptor {
	private final DatabaseClient databaseClient;

	public ExecuteInterceptor(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();
		Execute execute = method.getAnnotation(Execute.class);
		if (execute == null) {
			return methodInvocation.proceed();
		}
		String sql = execute.value();
		if (StringUtils.isBlank(sql)) {
			throw new RuntimeException("can't find sql statement for method:" + method.getName() + " of class:" + methodInvocation.getThis().getClass().getName());
		}
		DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
		Parameter[] parameters = method.getParameters();
		if (parameters != null && parameters.length > 0) {
			Object[] arguments = methodInvocation.getArguments();
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				Object parameterValue = null;
				if (arguments.length > i) {
					parameterValue = arguments[i];
				}
				if (parameterValue != null) {
					executeSpec = executeSpec.bind(parameter.getName(), parameterValue);
				} else {
					executeSpec = executeSpec.bindNull(parameter.getName(), parameter.getType());
				}
			}
		}
		Class<?> returnType = method.getReturnType();
		if (returnType.isAssignableFrom(Mono.class)) {
			return executeSpec.fetch().rowsUpdated();
		} else {
			return executeSpec;
		}
	}
}
