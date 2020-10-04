package org.loed.framework.mybatis.listener;


import org.loed.framework.mybatis.listener.spi.*;
import org.apache.commons.collections4.CollectionUtils;
import org.loed.framework.mybatis.listener.spi.*;

import java.util.ArrayList;
import java.util.Comparator;
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

	public static void registerPreInsertListeners(List<PreInsertListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (preInsertListeners == null) {
				preInsertListeners = new ArrayList<>();
			}
			preInsertListeners.addAll(listeners);
			preInsertListeners.sort(Comparator.comparing(PreInsertListener::getOrder));
		}
	}

	public static void registerPostInsertListeners(List<PostInsertListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (postInsertListeners == null) {
				postInsertListeners = new ArrayList<>();
			}
			postInsertListeners.addAll(listeners);
			postInsertListeners.sort(Comparator.comparing(PostInsertListener::getOrder));
		}
	}

	public static void registerPreUpdateListeners(List<PreUpdateListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (preUpdateListeners == null) {
				preUpdateListeners = new ArrayList<>();
			}
			preUpdateListeners.addAll(listeners);
			preUpdateListeners.sort(Comparator.comparing(PreUpdateListener::getOrder));
		}
	}

	public static void registerPostUpdateListeners(List<PostUpdateListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (postUpdateListeners == null) {
				postUpdateListeners = new ArrayList<>();
			}
			postUpdateListeners.addAll(listeners);
			postUpdateListeners.sort(Comparator.comparing(PostUpdateListener::getOrder));
		}
	}

	public static void registerPreDeleteListeners(List<PreDeleteListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (preDeleteListeners == null) {
				preDeleteListeners = new ArrayList<>();
			}
			preDeleteListeners.addAll(listeners);
			preDeleteListeners.sort(Comparator.comparing(PreDeleteListener::getOrder));
		}
	}

	public static void registerPostDeleteListeners(List<PostDeleteListener> listeners) {
		if (CollectionUtils.isNotEmpty(listeners)) {
			if (postDeleteListeners == null) {
				postDeleteListeners = new ArrayList<>();
			}
			postDeleteListeners.addAll(listeners);
			postDeleteListeners.sort(Comparator.comparing(PostDeleteListener::getOrder));
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
