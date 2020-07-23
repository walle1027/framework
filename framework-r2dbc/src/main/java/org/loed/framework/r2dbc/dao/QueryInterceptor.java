package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.r2dbc.query.Query;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/7/12 20:24
 */
public class QueryInterceptor<T, ID> implements MethodInterceptor {

	private final DatabaseClient databaseClient;

	private final BiFunction<Row, RowMetadata, T> mapper;

	private final Class<T> entityClass;

	public QueryInterceptor(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.mapper = new JPAClassRowMapper<>(entityClass);
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();
		Query query = method.getAnnotation(Query.class);
		if (query == null) {
			return methodInvocation.proceed();
		}
		String value = query.value();
		DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.execute(value);
		if (StringUtils.isBlank(value)) {
			throw new RuntimeException("error invoke method:");
		}
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
		Class<?> parameterizedReturnType = getParameterizedReturnType(method.getGenericReturnType());
		boolean assignableFromEntity = false;
		if (parameterizedReturnType != null && parameterizedReturnType.isAssignableFrom(entityClass)) {
			assignableFromEntity = true;
		}
		Class<?> returnType = method.getReturnType();
		if (returnType.isAssignableFrom(Mono.class)) {
			if (assignableFromEntity) {
				return executeSpec.map(mapper).one();
			} else if (parameterizedReturnType != null) {
				return executeSpec.as(parameterizedReturnType).fetch().one();
			} else {
				return executeSpec.map(mapper).one();
			}
		} else if (returnType.isAssignableFrom(Flux.class)) {
			if (assignableFromEntity) {
				return executeSpec.map(mapper).all();
			} else if (parameterizedReturnType != null) {
				return executeSpec.as(parameterizedReturnType).fetch().all();
			} else {
				return executeSpec.fetch().all();
			}
		} else {
			return executeSpec;
		}
	}

	private Class<?> getParameterizedReturnType(Type genericReturnType) {
		if (genericReturnType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
		}
		return null;
	}
}
