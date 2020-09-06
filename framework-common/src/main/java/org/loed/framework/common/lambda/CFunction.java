package org.loed.framework.common.lambda;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/4 11:22 上午
 */
@FunctionalInterface
public interface CFunction<T, R> extends Serializable {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	Collection<R> apply(T t);
}
