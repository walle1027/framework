package org.loed.framework.mybatis.listener.spi;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:38
 */
public interface PostDeleteListener {
	void postDelete(Object object);
}
