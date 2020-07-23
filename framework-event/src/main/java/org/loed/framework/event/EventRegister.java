package org.loed.framework.event;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015/8/7 9:54
 */

public class EventRegister implements InitializingBean {
	private EventManager eventManager;
	private Map<String, List<EventListener>> registerMap = new HashMap<>();

	public EventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public Map<String, List<EventListener>> getRegisterMap() {
		return registerMap;
	}

	public void setRegisterMap(Map<String, List<EventListener>> registerMap) {
		this.registerMap = registerMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (eventManager == null) {
			return;
		}
		if (registerMap.isEmpty()) {
			return;
		}
		for (Map.Entry<String, List<EventListener>> entry : registerMap.entrySet()) {
			String key = entry.getKey();
			List<EventListener> listeners = entry.getValue();
			if (CollectionUtils.isNotEmpty(listeners)) {
				for (EventListener listener : listeners) {
					eventManager.subscribe(key, listener);
				}
			}
		}
	}
}
