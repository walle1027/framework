package org.loed.framework.mybatis.listener.spi;

import org.springframework.core.Ordered;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:38
 */
public interface PreDeleteListener extends Ordered {
	boolean preDelete(Object object);
}
