package org.loed.framework.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/24 18:39
 */
public class CopyOnWriteMap<K, V> implements Map<K, V>, Cloneable {
	private final Lock lock = new ReentrantLock();
	private volatile Map<K, V> internalMap;

	public CopyOnWriteMap() {
		internalMap = new HashMap<K, V>();
	}

	//----------------------读方法-----------------------------//
	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}

	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return internalMap.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return internalMap.entrySet();
	}

	@Override
	public V get(Object key) {
		return internalMap.get(key);
	}

	//----------------------写方法-----------------------------//
	@Override
	public V put(K key, V value) {
		Lock lock = CopyOnWriteMap.this.lock;
		try {
			lock.lock();
			Map<K, V> newMap = new HashMap<>(internalMap);
			V put = newMap.put(key, value);
			internalMap = newMap;
			return put;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V remove(Object key) {
		Lock lock = CopyOnWriteMap.this.lock;
		try {
			lock.lock();
			Map<K, V> newMap = new HashMap<>(internalMap);
			V remove = newMap.remove(key);
			internalMap = newMap;
			return remove;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		Lock lock = CopyOnWriteMap.this.lock;
		try {
			lock.lock();
			Map<K, V> newMap = new HashMap<>(internalMap);
			if (m != null) {
				newMap.putAll(m);
			}
			internalMap = newMap;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		Lock lock = CopyOnWriteMap.this.lock;
		try {
			lock.lock();
			internalMap = new HashMap<>(0);
		} finally {
			lock.unlock();
		}
	}
}
