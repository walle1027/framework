package org.loed.framework.event.support.disruptor;


import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/1/20 14:14
 */
public class EventTranslator {
	private static final EventTranslatorOneArg<Event, String> TRANSLATOR =
			(event, sequence, context) -> event.setContext(context);
	private final RingBuffer<Event> ringBuffer;


	public EventTranslator(RingBuffer<Event> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void publish(String context) {
		ringBuffer.publishEvent(TRANSLATOR, context);
	}
}
