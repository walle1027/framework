package org.loed.framework.mybatis.listener.impl;


import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.po.LastModifyBy;
import org.loed.framework.common.po.LastModifyTime;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.mybatis.listener.spi.PreUpdateListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:47
 */
@Slf4j
public class DefaultPreUpdateListener implements PreUpdateListener {
	@Override
	public boolean preUpdate(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof List) {
			for (Object o : (List<?>) object) {
				setPropsForUpdate(o);
			}
		} else {
			setPropsForUpdate(object);
		}
		return true;
//		if (object == null) {
//			return true;
//		}
//		if (object instanceof CommonPO) {
//			CommonPO po = (CommonPO) object;
//			if (po.getUpdateBy() == null) {
//				po.setUpdateBy(SystemContextHolder.getAccountId());
//			}
//			if (po.getUpdateTime() == null) {
//				po.setUpdateTime(new Date());
//			}
//		}
//		if (object instanceof AutoGenIdPO) {
//			AutoGenIdPO po = (AutoGenIdPO) object;
//			if (po.getUpdateTime() == null) {
//				po.setUpdateTime(new Date());
//			}
//			if (po.getCreateBy() == null) {
//				try {
//					po.setUpdateBy(Long.valueOf(SystemContextHolder.getAccountId()));
//				} catch (Exception e) {
//					//TODO
//				}
//			}
//		}
//		return true;
	}

	private void setPropsForUpdate(Object object) {
		List<Field> fields = ReflectionUtils.getDeclaredFields(object);
		for (Field field : fields) {
			Class<?> type = field.getType();
			LastModifyBy lastModifyBy = field.getAnnotation(LastModifyBy.class);
			LastModifyTime lastModifyTime = field.getAnnotation(LastModifyTime.class);
			if (lastModifyBy != null) {
				if (isValidType(lastModifyBy.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @LastModifyBy supported types:" +
							Arrays.stream(lastModifyBy.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				if (type.getName().equals(Long.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, Long.valueOf(SystemContextHolder.getUserId()));
				} else if (type.getName().equals(String.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getUserId());
				} else if (type.getName().equals(BigInteger.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, BigInteger.valueOf(Long.parseLong(SystemContextHolder.getUserId())));
				}
			} else if (lastModifyTime != null) {
				if (isValidType(lastModifyTime.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @LastModifyTime supported types:" +
							Arrays.stream(lastModifyTime.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				if (type.getName().equals(Date.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, new Date());
				} else if (type.getName().equals(java.sql.Date.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
				} else if (type.getName().equals(LocalDateTime.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, LocalDateTime.now());
				}
			}
		}
	}

	private boolean isValidType(Class<?>[] classes, Class<?> type) {
		for (Class<?> clazz : classes) {
			if (clazz.getName().equals(type.getName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
