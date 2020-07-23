package org.loed.framework.event.support.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.loed.framework.event.EventListener;
import org.loed.framework.event.EventManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/1/20 14:18
 */
public class DisruptorEventManager implements EventManager {

	private ConcurrentHashMap<String, Disruptor<Event>> disruptorMap = new ConcurrentHashMap<String, Disruptor<Event>>();
	private Lock lock = new ReentrantLock();

	public Disruptor<Event> getDisruptor(String topic) {
		return disruptorMap.get(topic);
	}

	public void start(String topic) {
		Disruptor<Event> disruptor = disruptorMap.get(topic);
		if (disruptor != null) {
			disruptor.start();
		}
	}

	@Override
	public void subscribe(final String eventName, final EventListener listener) {
		Disruptor<Event> disruptor = _createDisruptorWithTopic(eventName);
//		disruptor.shutdown();
		disruptor.handleEventsWith(new EventHandler(eventName, listener));
		disruptor.start();
	}

	@Override
	public void publish(String topic, String context) {
		Disruptor<Event> disruptor = disruptorMap.get(topic);
		RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();
		EventTranslator translator = new EventTranslator(ringBuffer);
		translator.publish(context);
	}

	private Disruptor<Event> _createDisruptorWithTopic(String topic) {
		if (disruptorMap.get(topic) != null) {
			return disruptorMap.get(topic);
		}
		try {
			lock.lock();
			if (disruptorMap.get(topic) != null) {
				return disruptorMap.get(topic);
			}
			// Specify the size of the ring buffer, must be power of 2.
			int bufferSize = 1024;
			// Construct the Disruptor
			Disruptor<Event> disruptor = new Disruptor<>(Event::new, bufferSize, DaemonThreadFactory.INSTANCE);
			disruptorMap.put(topic, disruptor);
			return disruptor;
		} finally {
			lock.unlock();
		}
	}
}
