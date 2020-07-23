//package org.loed.framework.r2dbc.listener.impl;
//
//import org.loed.framework.common.SystemConstant;
//import org.loed.framework.common.context.SystemContext;
//import org.loed.framework.common.po.BasePO;
//import org.loed.framework.common.po.CommonPO;
//import org.loed.framework.r2dbc.listener.spi.PreUpdateListener;
//import org.springframework.web.server.ServerWebExchange;
//
//import java.util.Date;
//
///**
// * @author thomason
// * @version 1.0
// * @since 2018/1/2 上午10:47
// */
//public class DefaultPreUpdateListener implements PreUpdateListener {
//	@Override
//	public boolean preUpdate(ServerWebExchange exchange, Object object) {
//		if (object == null) {
//			return false;
//		}
//		if (object instanceof CommonPO) {
//			CommonPO po = (CommonPO) object;
//			if (po.getUpdateBy() == null) {
//				po.setUpdateBy(SystemContext.getAccountId());
//			}
//			if (po.getUpdateTime() == null) {
//				po.setUpdateTime(new Date());
//			}
//			if (po.getUpdateTime() == null) {
//				po.setUpdateTime(new Date());
//			}
//			if (po.getIsDeleted() == null) {
//				po.setIsDeleted(0);
//			}
//		}
//		if (object instanceof BasePO) {
//			((BasePO) object).setTenantCode(SystemContext.getTenantCode());
//		}
//		return true;
//	}
//
//	private Integer order;
//
//	public void setOrder(Integer order) {
//		this.order = order;
//	}
//	@Override
//	public int getOrder() {
//		return order == null ? -1 : order;
//	}
//}
