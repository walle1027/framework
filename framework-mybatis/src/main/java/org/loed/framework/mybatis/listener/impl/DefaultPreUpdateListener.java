package org.loed.framework.mybatis.listener.impl;


import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.po.LastModifyBy;
import org.loed.framework.common.po.LastModifyTime;
import org.loed.framework.common.util.ReflectionUtils;
import org.loed.framework.mybatis.listener.spi.PreUpdateListener;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:47
 */
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
			if (field.getAnnotation(LastModifyBy.class) != null) {
				ReflectionUtils.setFieldValue(object, field, SystemContextHolder.getAccountId());
			} else if (field.getAnnotation(LastModifyTime.class) != null) {
				ReflectionUtils.setFieldValue(object, field, new Date());
			}
		}
	}
}
