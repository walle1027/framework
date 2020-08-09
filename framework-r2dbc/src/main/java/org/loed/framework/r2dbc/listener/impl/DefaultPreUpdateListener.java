package org.loed.framework.r2dbc.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.po.LastModifyBy;
import org.loed.framework.common.po.LastModifyTime;
import org.loed.framework.common.util.LocalDateUtils;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.spi.PreUpdateListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 9:59 上午
 */
@Slf4j
public class DefaultPreUpdateListener implements PreUpdateListener {
	@Override
	public <T> Mono<T> preUpdate(T object) {
		if (object == null) {
			return Mono.empty();
		}
		List<Field> fields = ReflectionUtils.getDeclaredFields(object.getClass());
		Flux<Boolean> filedFlux;
		if (fields.size() > 0) {
			filedFlux = Flux.fromIterable(fields).flatMap(field -> {
				if (field.getAnnotation(LastModifyBy.class) != null) {
					return ReactiveSystemContext.getAccountId().map(accountId -> {
						ReflectionUtils.setFieldValue(object, field, accountId);
						return true;
					});
				} else if (field.getAnnotation(LastModifyTime.class) != null) {
					Date now = new Date();
					if (field.getType().getName().equals(Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, now);
					} else if (field.getType().getName().equals(java.sql.Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
					} else if (field.getType().getName().equals(LocalDateTime.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, LocalDateUtils.convertDateToLDT(new Date()));
					} else if (field.getType().getName().equals(LocalDate.class.getName())) {
						LocalDateTime localDateTime = LocalDateUtils.convertDateToLDT(new Date());
						ReflectionUtils.setFieldValue(object, field, localDateTime.toLocalDate());
					} else {
						log.warn("filed:" + field.getName() + " has CreateTime annotation but type is :" + field.getType().getName() + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
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
		if (methods.size() > 0) {
			methodFlux = Flux.fromIterable(methods).flatMap(method -> {
				if (method.getAnnotation(LastModifyBy.class) != null) {
					return ReactiveSystemContext.getAccountId().map(accountId -> {
						try {
							method.invoke(object, accountId);
						} catch (IllegalAccessException | InvocationTargetException e) {
							log.error("error invoke method:" + method.getName() + " of class : " + object.getClass() + " caused by " + e.getMessage(), e);
						}
						return true;
					});
				} else if (method.getAnnotation(LastModifyTime.class) != null) {
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
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDateUtils.convertDateToLDT(now)});
					} else if (parameterTypeName.equals(LocalDate.class.getName())) {
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDateUtils.convertDateToLDT(now).toLocalDate()});
					} else {
						log.warn("method:" + method.getName() + " has CreateTime annotation but type is :" + parameterTypeName + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
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
}
