package org.loed.framework.r2dbc.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.data.DataType;
import org.loed.framework.common.po.LastModifyBy;
import org.loed.framework.common.po.LastModifyTime;
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
					return ReactiveSystemContext.getSystemContext().defaultIfEmpty(new SystemContext()).map(context -> {
						if (context.getUserId() != null) {
							//TODO convert type
							try {
								int targetType = DataType.getDataType(field.getType());
								Object convertedValue = DataType.toType(context.getUserId(), DataType.DT_String, targetType);
								ReflectionUtils.setFieldValue(object, field, convertedValue);
							} catch (Exception e) {
								log.error("error set LastModifyBy for field:" + field.getName() + " caused by: " + e.getMessage(), e);
							}
						}
						return true;
					});
				} else if (field.getAnnotation(LastModifyTime.class) != null) {
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
			//compose this is the setter methods
			methodFlux = Flux.fromIterable(methods).flatMap(method -> {
				if (method.getAnnotation(LastModifyBy.class) != null) {
					return ReactiveSystemContext.getSystemContext().defaultIfEmpty(new SystemContext()).map(context -> {
						try {
							if (context.getUserId() != null) {
								int targetType = DataType.getDataType(method.getParameterTypes()[0]);
								Object convertedValue = DataType.toType(context.getUserId(), DataType.DT_String, targetType);
								method.invoke(object, convertedValue);
							}
						} catch (Exception e) {
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
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDateTime.now()});
					} else if (parameterTypeName.equals(LocalDate.class.getName())) {
						ReflectionUtils.invokeMethod(object, method.getName(), method.getParameterTypes(), new Object[]{LocalDate.now()});
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
