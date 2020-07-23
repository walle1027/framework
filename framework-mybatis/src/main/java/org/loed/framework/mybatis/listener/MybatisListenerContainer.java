package org.loed.framework.mybatis.listener;


import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.mybatis.listener.spi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:42
 */
public class MybatisListenerContainer {
	private static List<PreInsertListener> preInsertListeners;
	private static List<PreUpdateListener> preUpdateListeners;
	private static List<PreDeleteListener> preDeleteListeners;
	private static List<PostInsertListener> postInsertListeners;
	private static List<PostUpdateListener> postUpdateListeners;
	private static List<PostDeleteListener> postDeleteListeners;

	public static void addPreInsertListeners(List<PreInsertListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (preInsertListeners == null) {
					preInsertListeners = new ArrayList<>();
				}
				preInsertListeners.add((PreInsertListener) listener);
			}
		}
	}

	public static void addPreUpdateListeners(List<PreUpdateListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (preUpdateListeners == null) {
					preUpdateListeners = new ArrayList<>();
				}
				preUpdateListeners.add((PreUpdateListener) listener);
			}
		}
	}

	public static void addPreDeleteListeners(List<PreDeleteListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (preDeleteListeners == null) {
					preDeleteListeners = new ArrayList<>();
				}
				preDeleteListeners.add((PreDeleteListener) listener);
			}
		}
	}

	public static void addPostInsertListeners(List<PostInsertListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (postInsertListeners == null) {
					postInsertListeners = new ArrayList<>();
				}
				postInsertListeners.add((PostInsertListener) listener);
			}
		}
	}

	public static void registerPostUpdateListeners(List<PostUpdateListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (postUpdateListeners == null) {
					postUpdateListeners = new ArrayList<>();
				}
				postUpdateListeners.add((PostUpdateListener) listener);
			}
		}
	}

	public static void registerPostDeleteListeners(List<PostDeleteListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			for (Object listener : listeners) {
				if (postDeleteListeners == null) {
					postDeleteListeners = new ArrayList<>();
				}
				postDeleteListeners.add((PostDeleteListener) listener);
			}
		}
	}

	public static List<PreInsertListener> getPreInsertListeners() {
		return preInsertListeners;
	}

	public static List<PreUpdateListener> getPreUpdateListeners() {
		return preUpdateListeners;
	}

	public static List<PreDeleteListener> getPreDeleteListeners() {
		return preDeleteListeners;
	}

	public static List<PostInsertListener> getPostInsertListeners() {
		return postInsertListeners;
	}

	public static List<PostUpdateListener> getPostUpdateListeners() {
		return postUpdateListeners;
	}

	public static List<PostDeleteListener> getPostDeleteListeners() {
		return postDeleteListeners;
	}
}
