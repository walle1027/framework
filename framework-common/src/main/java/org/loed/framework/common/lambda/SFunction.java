package org.loed.framework.common.lambda;

import java.io.Serializable;

@FunctionalInterface
public interface SFunction<T, R> extends  Serializable {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	R apply(T t);
}
