package org.loed.framework.common.balancer;

import java.util.Collection;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/6/30 9:17
 */
public interface Balancer<T extends Balanceable> {
	T select();

	void updateProfiles(Collection<T> nodeList);

	void setWhiteList(Collection<String> whiteList);
}
