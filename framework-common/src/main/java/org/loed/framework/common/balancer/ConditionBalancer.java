/**
 *
 */
package org.loed.framework.common.balancer;

/**
 * @param <C>
 * @param <T>
 * @author
 */
public interface ConditionBalancer<T extends Balanceable, C> extends Balancer<T> {

	T select(C condition);
}
