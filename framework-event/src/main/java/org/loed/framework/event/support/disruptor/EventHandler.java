package org.loed.framework.event.support.disruptor;

import org.loed.framework.event.EventListener;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/1/20 14:13
 */
public class EventHandler implements com.lmax.disruptor.EventHandler<Event> {
	private String topic;
	private EventListener listener;

	public EventHandler(String topic, EventListener listener) {
		this.topic = topic;
		this.listener = listener;
	}

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		String context = event.getContext();
		listener.execute(topic, context);
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public EventListener getListener() {
		return listener;
	}

	public void setListener(EventListener listener) {
		this.listener = listener;
	}
}
