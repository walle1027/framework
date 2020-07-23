package org.loed.framework.r2dbc.listener;

import org.springframework.core.Ordered;

public interface OrderedListener extends Ordered {
    @Override
    int getOrder();
}
