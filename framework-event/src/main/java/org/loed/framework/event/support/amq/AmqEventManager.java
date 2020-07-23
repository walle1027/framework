package org.loed.framework.event.support.amq;

import org.loed.framework.event.EventListener;
import org.loed.framework.event.EventManager;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/7/17 22:20
 */
public class AmqEventManager implements EventManager {
	@Override
	public void subscribe(String eventName, EventListener listener) {
		//TODO
	}

	@Override
	public void publish(String topic, String context) {
		//TODO
	}
}
