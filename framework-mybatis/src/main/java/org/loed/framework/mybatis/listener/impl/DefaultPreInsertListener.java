package org.loed.framework.mybatis.listener.impl;


import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.po.CreateBy;
import org.loed.framework.common.po.CreateTime;
import org.loed.framework.common.po.IsDeleted;
import org.loed.framework.common.po.TenantId;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.mybatis.listener.spi.PreInsertListener;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:43
 */
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
			if (field.getAnnotation(Id.class) != null) {
				GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
				if (generatedValue == null || generatedValue.strategy() == GenerationType.IDENTITY) {
					Object fieldValue = ReflectionUtils.getFieldValue(object, field);
					if (fieldValue == null) {
						ReflectionUtils.setFieldValue(object, field, UUIDUtils.getUUID());
					}
				}
			} else if (field.getAnnotation(CreateBy.class) != null) {
				ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getAccountId());
			} else if (field.getAnnotation(CreateTime.class) != null) {
				ReflectionUtils.setFieldValue(object, field, new Date());
			} else if (field.getAnnotation(Version.class) != null) {
				ReflectionUtils.setFieldValue(object, field, 1L);
			} else if (field.getAnnotation(IsDeleted.class) != null) {
				//TODO 枚举类型
				ReflectionUtils.setFieldValue(object, field, 0);
			} else if (field.getAnnotation(TenantId.class) != null) {
				ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getTenantCode());
			}
		}
	}
}
