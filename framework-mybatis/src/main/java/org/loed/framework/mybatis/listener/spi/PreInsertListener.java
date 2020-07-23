package org.loed.framework.mybatis.listener.spi;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:26
 */
public interface PreInsertListener {
	boolean preInsert(Object object);
}