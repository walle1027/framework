package org.loed.framework.mybatis.listener.impl;


import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.po.CreateBy;
import org.loed.framework.common.po.CreateTime;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.mybatis.listener.spi.PreInsertListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
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
 * @since 2018/1/2 上午10:43
 */
@Slf4j
public class DefaultPreInsertListener implements PreInsertListener {
	@Override
	public boolean preInsert(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof List) {
			for (Object o : (List<?>) object) {
				setPropsForInsert(o);
			}
		} else {
			setPropsForInsert(object);
		}
		return true;
	}

	private void setPropsForInsert(Object object) {
		List<Field> fields = ReflectionUtils.getDeclaredFields(object);
		for (Field field : fields) {
			Class<?> type = field.getType();
			CreateBy createBy = field.getAnnotation(CreateBy.class);
			CreateTime createTime = field.getAnnotation(CreateTime.class);
			IsDeleted isDeleted = field.getAnnotation(IsDeleted.class);
			TenantId tenantId = field.getAnnotation(TenantId.class);
			if (field.getAnnotation(Id.class) != null) {
				GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
				if (generatedValue == null || generatedValue.strategy() == GenerationType.IDENTITY) {
					Object fieldValue = ReflectionUtils.getFieldValue(object, field);
					if (fieldValue == null) {
						ReflectionUtils.setFieldValue(object, field, UUIDUtils.getUUID());
					}
				}
			} else if (createBy != null) {
				//做类型验证
				if (isValidType(createBy.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @CreateBy supported types:" +
							Arrays.stream(createBy.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				//不同数据类型 单独实现
				if (type.getName().equals(Long.class.getName()) || type.getName().equals(long.class.getName())) {
					if (StringUtils.isNotBlank(SystemContextHolder.getUserId())) {
						ReflectionUtils.setFieldValue(object, field, Long.valueOf(SystemContextHolder.getUserId()));
					}
				} else if (type.getName().equals(String.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getUserId());
				} else if (type.getName().equals(BigInteger.class.getName())) {
					if (StringUtils.isNotBlank(SystemContextHolder.getUserId())) {
						ReflectionUtils.setFieldValue(object, field, BigInteger.valueOf(Long.parseLong(SystemContextHolder.getUserId())));
					}
				}
			} else if (createTime != null) {
				//做类型验证
				if (isValidType(createTime.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @CreateTime supported types:" +
							Arrays.stream(createTime.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				if (type.getName().equals(Date.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, new Date());
				} else if (type.getName().equals(java.sql.Date.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, new java.sql.Date(System.currentTimeMillis()));
				} else if (type.getName().equals(LocalDateTime.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, LocalDateTime.now());
				}
			} else if (field.getAnnotation(Version.class) != null) {
				ReflectionUtils.setFieldValue(object, field, 0L);
			} else if (isDeleted != null) {
				//做类型验证
				if (isValidType(isDeleted.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @IsDeleted supported types:" +
							Arrays.stream(isDeleted.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				if (type.getName().equals(Integer.class.getName()) || type.getName().equals(int.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, 0);
				} else if (type.getName().equals(Byte.class.getName()) || type.getName().equals(byte.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, (byte) 0);
				} else if (type.getName().equals(Long.class.getName()) || type.getName().equals(long.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, 0L);
				} else {
					log.warn("field:" + field.getName() + " has @IsDeleted annotation but type is:" + type.getName() + " ,can't set any value");
				}
			} else if (tenantId != null) {
				//做类型验证
				if (isValidType(tenantId.supportedTypes(), type)) {
					log.warn("field type:" + type.getName() + " is not suitable for @TenantId supported types:" +
							Arrays.stream(tenantId.supportedTypes()).map(Class::getName).collect(Collectors.joining(",")));
					continue;
				}
				if (type.getName().equals(String.class.getName())) {
					ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getTenantCode());
				} else if (type.getName().equals(Long.class.getName())) {
					if (StringUtils.isNotBlank(SystemContextHolder.getUserId())) {
						ReflectionUtils.setFieldValue(object, field, Long.valueOf(SystemContextHolder.getTenantCode()));
					}
				} else if (type.getName().equals(BigInteger.class.getName())) {
					if (StringUtils.isNotBlank(SystemContextHolder.getUserId())) {
						ReflectionUtils.setFieldValue(object, field, BigInteger.valueOf(Long.parseLong(SystemContextHolder.getTenantCode())));
					}
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
