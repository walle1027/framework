package org.loed.framework.r2dbc.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.po.CreateBy;
import org.loed.framework.common.po.CreateTime;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.Version;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:43
 */
@Slf4j
public class DefaultPreInsertListener implements PreInsertListener {

	private int order = Ordered.LOWEST_PRECEDENCE;


	@Override
	public <T> Mono<T> preInsert(T object) {
		if (object == null) {
			return Mono.empty();
		}
		List<Field> fields = ReflectionUtils.getDeclaredFields(object.getClass());
		Flux<Boolean> filedFlux;
		if (fields.size() > 0) {
			filedFlux = Flux.fromIterable(fields).flatMap(field -> {
				if (field.getAnnotation(TenantId.class) != null) {
					return ReactiveSystemContext.getSystemContext().map(context -> {
						String tenantCode = context.getTenantCode();
						try {
							int targetType = DataType.getDataType(field.getType());
							Object convertedValue = DataType.toType(tenantCode, DataType.DT_String, targetType);
							ReflectionUtils.setFieldValue(object, field, convertedValue);
						} catch (Exception e) {
							log.error("error set LastModifyBy for field:" + field.getName() + " caused by: " + e.getMessage(), e);
						}
						return true;
					});
				} else if (field.getAnnotation(CreateBy.class) != null) {
					return ReactiveSystemContext.getSystemContext().map(context -> {
						String accountId = context.getAccountId();
						try {
							int targetType = DataType.getDataType(field.getType());
							Object convertedValue = DataType.toType(accountId, DataType.DT_String, targetType);
							ReflectionUtils.setFieldValue(object, field, convertedValue);
						} catch (Exception e) {
							log.error("error set LastModifyBy for field:" + field.getName() + " caused by: " + e.getMessage(), e);
						}
						return true;
					});
				} else if (field.getAnnotation(CreateTime.class) != null) {
					Date now = new Date();
					if (field.getType().getName().equals(Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, now);
					} else if (field.getType().getName().equals(java.sql.Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
					} else if (field.getType().getName().equals(LocalDateTime.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, LocalDateTime.now());
					} else if (field.getType().getName().equals(LocalDate.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, LocalDate.now());
					} else {
						log.warn("filed:" + field.getName() + " has CreateTime annotation but type is :" + field.getType().getName() + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
					}
					return Mono.just(true);
				} else if (field.getAnnotation(Version.class) != null) {
					if (field.getType().getName().equals(Long.class.getName()) || field.getType().getName().equals(long.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, 0L);
					} else if (field.getType().getName().equals(BigInteger.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, BigInteger.ZERO);
					} else {
						log.warn("filed:" + field.getName() + " has Version annotation but type is :" + field.getType().getName() + " is not java.lang.Long will not set value");
					}
					return Mono.just(true);
				} else if (field.getAnnotation(IsDeleted.class) != null) {
					if (field.getType().getName().equals(Integer.class.getName()) || field.getType().getName().equals(int.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, 0);
					} else if (field.getType().getName().equals(Byte.class.getName()) || field.getType().getName().equals(byte.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, (byte) 0);
					} else {
						log.warn("filed:" + field.getName() + " has Version annotation but type is :" + field.getType().getName() + " is not java.lang.Long will not set value");
					}
					return Mono.just(true);
				} else {
					return Mono.just(true);
				}
			});
		} else {
			filedFlux = Flux.just(true);
		}
		List<Method> methods = ReflectionUtils.getDeclaredMethods(object.getClass());
		Flux<Boolean> methodFlux;
		//compose this is setter method
		if (methods.size() > 0) {
			methodFlux = Flux.fromIterable(methods).flatMap(method -> {
				if (method.getAnnotation(TenantId.class) != null) {
					return ReactiveSystemContext.getSystemContext().map(context -> {
						try {
							String tenantCode = context.getTenantCode();
							int targetType = DataType.getDataType(method.getParameterTypes()[0]);
							Object convertedValue = DataType.toType(tenantCode, DataType.DT_String, targetType);
							method.invoke(object, convertedValue);
						} catch (IllegalAccessException | InvocationTargetException e) {
							log.error("error invoke method:" + method.getName() + " of class : " + object.getClass() + " caused by " + e.getMessage(), e);
						}
						return true;
					});
				} else if (method.getAnnotation(CreateBy.class) != null) {
					return ReactiveSystemContext.getSystemContext().map(context -> {
						try {
							String accountId = context.getAccountId();
							int targetType = DataType.getDataType(method.getParameterTypes()[0]);
							Object convertedValue = DataType.toType(accountId, DataType.DT_String, targetType);
							method.invoke(object, convertedValue);
						} catch (IllegalAccessException | InvocationTargetException e) {
							log.error("error invoke method:" + method.getName() + " of class : " + object.getClass() + " caused by " + e.getMessage(), e);
						}
						return true;
					});
				} else if (method.getAnnotation(CreateTime.class) != null) {
					Date now = new Date();
					Class<?> parameterType = method.getParameterTypes()[0];
					String parameterTypeName = parameterType.getName();
					if (parameterTypeName.equals(Date.class.getName())) {
						try {
							method.invoke(object, now);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					} else if (parameterTypeName.equals(java.sql.Date.class.getName())) {
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{new java.sql.Date(now.getTime())});
					} else if (parameterTypeName.equals(LocalDateTime.class.getName())) {
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDateTime.now()});
					} else if (parameterTypeName.equals(LocalDate.class.getName())) {
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDate.now()});
					} else {
						log.warn("method:" + method.getName() + " has CreateTime annotation but type is :" + parameterTypeName + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
					}
					return Mono.just(true);
				} else if (method.getAnnotation(Version.class) != null) {
					Class<?> parameterType = method.getParameterTypes()[0];
					if (parameterType.getName().equals(Long.class.getName()) || parameterType.getName().equals(long.class.getName())) {
						try {
							method.invoke(object, 0L);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					} else if (parameterType.getName().equals(BigInteger.class.getName())) {
						try {
							method.invoke(object, BigInteger.ZERO);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					} else {
						log.warn("filed:" + method.getName() + " has Version annotation but type is :" + parameterType.getName() + " is not java.lang.Long will not set value");
					}
					return Mono.just(true);
				} else {
					return Mono.just(true);
				}
			});
		} else {
			methodFlux = Flux.just(Boolean.TRUE);
		}
		return filedFlux.mergeWith(methodFlux).all(p -> p).thenReturn(object);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
