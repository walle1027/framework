package org.loed.framework.r2dbc.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.po.CreateBy;
import org.loed.framework.common.po.CreateTime;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.Version;
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
 * @since 2018/1/2 上午10:43
 */
@Slf4j
public class DefaultPreInsertListener implements PreInsertListener {

	@Override
	public Mono<Boolean> preInsert(Object object) {
		if (object == null) {
			return Mono.just(false);
		}
		List<Field> fields = ReflectionUtils.getDeclaredFields(object.getClass());
		Flux<Boolean> filedFlux;
		if (fields.size() > 0) {
			filedFlux = Flux.fromIterable(fields).flatMap(field -> {
				if (field.getAnnotation(TenantId.class) != null) {
					return ReactiveSystemContext.getTenantCode().map(tenentCode -> {
						ReflectionUtils.setFieldValue(object, field, tenentCode);
						return true;
					});
				} else if (field.getAnnotation(CreateBy.class) != null) {
					return ReactiveSystemContext.getAccountId().map(accountId -> {
						ReflectionUtils.setFieldValue(object, field, accountId);
						return true;
					});
				} else if (field.getAnnotation(CreateTime.class) != null) {
					Date now = new Date();
					if (field.getType().getName().equals(Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, now);
					} else if (field.getType().getName().equals(java.sql.Date.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
					} else if (field.getType().getName().equals(LocalDateTime.class.getName())) {
//						ReflectionUtils.setFieldValue(object,field,);
					} else if (field.getType().getName().equals(LocalDate.class.getName())) {

					} else {
						log.warn("filed:" + field.getName() + " has CreateTime annotation but type is :" + field.getType().getName() + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
					}
					return Mono.just(true);
				} else if (field.getAnnotation(Version.class) != null) {
					if (field.getType().getName().equals(Long.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, 0L);
					} else {
						log.warn("filed:" + field.getName() + " has Version annotation but type is :" + field.getType().getName() + " is not java.lang.Long will not set value");
					}
					return Mono.just(true);
				} else if (field.getAnnotation(IsDeleted.class) != null) {
					if (field.getType().getName().equals(Integer.class.getName())) {
						ReflectionUtils.setFieldValue(object, field, 0);
					} else if (field.getType().getName().equals(Byte.class.getName())) {
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
		if (methods.size() > 0) {
			methodFlux = Flux.fromIterable(methods).flatMap(method -> {
				if (method.getAnnotation(TenantId.class) != null) {
					return ReactiveSystemContext.getTenantCode().map(tenantCode -> {
						try {
							method.invoke(object, tenantCode);
						} catch (IllegalAccessException | InvocationTargetException e) {
							log.error("error invoke method:" + method.getName() + " of class : " + object.getClass() + " caused by " + e.getMessage(), e);
						}
						return true;
					});
				} else if (method.getAnnotation(CreateBy.class) != null) {
					return ReactiveSystemContext.getAccountId().map(accountId -> {
						try {
							method.invoke(object, accountId);
						} catch (IllegalAccessException | InvocationTargetException e) {
							log.error("error invoke method:" + method.getName() + " of class : " + object.getClass() + " caused by " + e.getMessage(), e);
						}
						return true;
					});
				} else if (method.getAnnotation(CreateTime.class) != null) {
					Date now = new Date();
					Class<?> parameterType = method.getParameterTypes()[0];
					if (parameterType.getName().equals(Date.class.getName())) {
						try {
							method.invoke(object, now);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
//					} else if (field.getType().getName().equals(java.sql.Date.class.getName())) {
//						ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
//					} else if (field.getType().getName().equals(LocalDateTime.class.getName())) {
////						ReflectionUtils.setFieldValue(object,field,);
//					} else if (field.getType().getName().equals(LocalDate.class.getName())) {

					} else {
						log.warn("method:" + method.getName() + " has CreateTime annotation but type is :" + parameterType.getName() + " is not one of the [java.util.Date" +
								",java.sql.Date,java.time.LocalDateTime,java.time.LocalDate] will not set value");
					}
					return Mono.just(true);
				} else if (method.getAnnotation(Version.class) != null) {
					Class<?> parameterType = method.getParameterTypes()[0];
					if (parameterType.getName().equals(Long.class.getName())) {
						try {
							method.invoke(object, 0L);
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
		return filedFlux.mergeWith(methodFlux).all(p -> p);
	}

	private Integer order;

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order == null ? -1 : order;
	}
}
