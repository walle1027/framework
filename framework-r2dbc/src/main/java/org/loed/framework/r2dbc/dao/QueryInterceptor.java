package org.loed.framework.r2dbc.dao;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.r2dbc.R2dbcException;
import org.loed.framework.r2dbc.query.Query;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/7/12 20:24
 */
@Slf4j
public class QueryInterceptor<T, ID> implements MethodInterceptor {

	private final DatabaseClient databaseClient;

	private final BiFunction<Row, RowMetadata, T> mapper;

	private final Class<T> entityClass;

	private ConversionService conversionService;

	private Class<?>[] supportedSimpleTypes;

	public QueryInterceptor(Class<? extends R2dbcDao<T, ID>> daoInterface, DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
		this.entityClass = (Class<T>) ((ParameterizedType) daoInterface.getGenericInterfaces()[0]).getActualTypeArguments()[0];
		this.mapper = new JPAClassRowMapper<>(entityClass);
		this.conversionService = new DefaultConversionService();
		supportedSimpleTypes = new Class<?>[]{
				String.class, char.class, Integer.class, int.class, Long.class, long.class,
				Double.class, double.class, Float.class, float.class, LocalDateTime.class, LocalDate.class,
				BigDecimal.class, BigInteger.class, boolean.class, Boolean.class
		};
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();
		Query query = method.getAnnotation(Query.class);
		if (query == null) {
			return methodInvocation.proceed();
		}
		String sql = query.value();
		DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
		if (StringUtils.isBlank(sql)) {
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
			} else if (parameterizedReturnType != null && supportType(parameterizedReturnType)) {
//				return executeSpec.fetch().one();
				return executeSpec.fetch().one().flatMap(row -> {
					Collection<Object> values = row.values();
					for (Object value : values) {
						if (value == null) {
							return Mono.empty();
						}
						if (getConversionService().canConvert(value.getClass(), parameterizedReturnType)) {
							Object convertedValue = getConversionService().convert(value, parameterizedReturnType);
							if (convertedValue != null) {
								return Mono.just(convertedValue);
							}
							return Mono.error(new R2dbcException("convert null from value:" + values + " for type " + parameterizedReturnType));
						}
					}
					return Mono.error(new R2dbcException("can't convert value:" + values + " to type " + parameterizedReturnType));
				});
			} else {
				return executeSpec.fetch().one();
			}
		} else if (returnType.isAssignableFrom(Flux.class)) {
			if (assignableFromEntity) {
				return executeSpec.map(mapper).all();
			} else if (parameterizedReturnType != null && supportType(parameterizedReturnType)) {
				return executeSpec.fetch().all().flatMap(row -> {
					Collection<Object> values = row.values();
					for (Object value : values) {
						if (value == null) {
							return Flux.empty();
						}
						if (getConversionService().canConvert(value.getClass(), parameterizedReturnType)) {
							Object convertedValue = getConversionService().convert(value, parameterizedReturnType);
							if (convertedValue != null) {
								return Flux.just(convertedValue);
							}
							return Flux.error(new R2dbcException("convert null from value:" + values + " for type " + parameterizedReturnType));
						}
					}
					return Flux.error(new R2dbcException("can't convert value:" + values + " to type " + parameterizedReturnType));
				});
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

	protected ConversionService getConversionService() {
		return conversionService;
	}

	protected boolean supportType(Class<?> type) {
		for (Class<?> supportedSimpleType : supportedSimpleTypes) {
			if (supportedSimpleType.getName().equals(type.getName())) {
				return true;
			}
		}
		return false;
	}
}
