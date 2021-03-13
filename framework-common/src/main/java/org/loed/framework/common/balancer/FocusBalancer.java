package org.loed.framework.common.balancer;

import java.util.Collection;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/3/12 6:48 下午
 */
public class FocusBalancer<T extends Balanceable> implements Balancer<T> {
	private T focus;

	private final Balancer<T> delegate;

	public FocusBalancer(T focus, Balancer<T> delegate) {
		this.focus = focus;
		this.delegate = delegate;
	}

	public T getFocus() {
		return this.focus;
	}

	@Override
	public T select() {
		return this.delegate.select();
	}

	@Override
	public void updateProfiles(Collection<T> nodeList) {
		this.delegate.updateProfiles(nodeList);
	}

	@Override
	public void setWhiteList(Collection<String> whiteList) {
		this.delegate.setWhiteList(whiteList);
	}

	public void updateFocus(T focus) {
		this.focus = focus;
	}
}
