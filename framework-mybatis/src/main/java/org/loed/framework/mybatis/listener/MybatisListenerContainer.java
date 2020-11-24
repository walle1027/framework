package org.loed.framework.mybatis.listener;


import org.loed.framework.common.SpringUtils;
import org.loed.framework.mybatis.listener.spi.*;
import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/2 上午10:42
 */
public class MybatisListenerContainer {
//	private static List<PreInsertListener> preInsertListeners;
//	private static List<PreUpdateListener> preUpdateListeners;
//	private static List<PreDeleteListener> preDeleteListeners;
//	private static List<PostInsertListener> postInsertListeners;
//	private static List<PostUpdateListener> postUpdateListeners;
//	private static List<PostDeleteListener> postDeleteListeners;
//
//	public static void registerPreInsertListeners(List<PreInsertListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (preInsertListeners == null) {
//				preInsertListeners = new ArrayList<>();
//			}
//			preInsertListeners.addAll(listeners);
//			preInsertListeners.sort(Comparator.comparing(PreInsertListener::getOrder));
//		}
//	}
//
//	public static void registerPostInsertListeners(List<PostInsertListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (postInsertListeners == null) {
//				postInsertListeners = new ArrayList<>();
//			}
//			postInsertListeners.addAll(listeners);
//			postInsertListeners.sort(Comparator.comparing(PostInsertListener::getOrder));
//		}
//	}
//
//	public static void registerPreUpdateListeners(List<PreUpdateListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (preUpdateListeners == null) {
//				preUpdateListeners = new ArrayList<>();
//			}
//			preUpdateListeners.addAll(listeners);
//			preUpdateListeners.sort(Comparator.comparing(PreUpdateListener::getOrder));
//		}
//	}
//
//	public static void registerPostUpdateListeners(List<PostUpdateListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (postUpdateListeners == null) {
//				postUpdateListeners = new ArrayList<>();
//			}
//			postUpdateListeners.addAll(listeners);
//			postUpdateListeners.sort(Comparator.comparing(PostUpdateListener::getOrder));
//		}
//	}
//
//	public static void registerPreDeleteListeners(List<PreDeleteListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (preDeleteListeners == null) {
//				preDeleteListeners = new ArrayList<>();
//			}
//			preDeleteListeners.addAll(listeners);
//			preDeleteListeners.sort(Comparator.comparing(PreDeleteListener::getOrder));
//		}
//	}
//
//	public static void registerPostDeleteListeners(List<PostDeleteListener> listeners) {
//		if (CollectionUtils.isNotEmpty(listeners)) {
//			if (postDeleteListeners == null) {
//				postDeleteListeners = new ArrayList<>();
//			}
//			postDeleteListeners.addAll(listeners);
//			postDeleteListeners.sort(Comparator.comparing(PostDeleteListener::getOrder));
//		}
//	}

	public static List<PreInsertListener> getPreInsertListeners() {
		Map<String, PreInsertListener> beans = SpringUtils.applicationContext.getBeansOfType(PreInsertListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}

	public static List<PreUpdateListener> getPreUpdateListeners() {
		Map<String, PreUpdateListener> beans = SpringUtils.applicationContext.getBeansOfType(PreUpdateListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}

	public static List<PreDeleteListener> getPreDeleteListeners() {
		Map<String, PreDeleteListener> beans = SpringUtils.applicationContext.getBeansOfType(PreDeleteListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}

	public static List<PostInsertListener> getPostInsertListeners() {
		Map<String, PostInsertListener> beans = SpringUtils.applicationContext.getBeansOfType(PostInsertListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}

	public static List<PostUpdateListener> getPostUpdateListeners() {
		Map<String, PostUpdateListener> beans = SpringUtils.applicationContext.getBeansOfType(PostUpdateListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}

	public static List<PostDeleteListener> getPostDeleteListeners() {
		Map<String, PostDeleteListener> beans = SpringUtils.applicationContext.getBeansOfType(PostDeleteListener.class);
		if (beans.isEmpty()) {
			return null;
		}
		return beans.values().stream().sorted(Comparator.comparing(Ordered::getOrder)).collect(Collectors.toList());
	}
}
