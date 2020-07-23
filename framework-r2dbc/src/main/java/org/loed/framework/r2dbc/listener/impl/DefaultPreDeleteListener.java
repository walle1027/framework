package org.loed.framework.r2dbc.listener.impl;

import org.loed.framework.r2dbc.listener.spi.PreDeleteListener;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:47
 */
public class DefaultPreDeleteListener implements PreDeleteListener {

	@Override
	public boolean preDelete(ServerWebExchange exchange, Object object) {
		return true;
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
