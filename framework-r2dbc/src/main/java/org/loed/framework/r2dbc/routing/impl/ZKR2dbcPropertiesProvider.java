package org.loed.framework.r2dbc.routing.impl;

import org.loed.framework.common.CopyOnWriteMap;
import org.loed.framework.r2dbc.routing.R2dbcPropertiesProvider;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/10 6:58 下午
 */
public class ZKR2dbcPropertiesProvider implements R2dbcPropertiesProvider {
	private final String zkAddress;

	private CopyOnWriteMap<String,R2dbcProperties> propertiesCopyOnWriteMap;

	public ZKR2dbcPropertiesProvider(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	@Override
	public R2dbcProperties getR2dbcProperties(Object routingKey, String routingValue) {
		return null;
	}

	@Override
	public List<R2dbcProperties> getAllProperties() {
		return null;
	}
}
