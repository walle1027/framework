package org.loed.framework.r2dbc.listener;

import org.springframework.core.Ordered;

public interface OrderedListener extends Ordered {
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
