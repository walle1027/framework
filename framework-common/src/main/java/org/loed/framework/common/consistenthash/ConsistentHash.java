package org.loed.framework.common.consistenthash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Thomason
 * @version 1.0
 * @since 13-5-29 下午6:20
 */

public class ConsistentHash<T extends ConsistentHashNode> {
	protected final SortedMap<Integer, T> circle = new TreeMap<Integer, T>();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected HashFunction hashFunction;
	protected int numberOfReplicas = 1;

	public ConsistentHash() {
	}

	public ConsistentHash(HashFunction hashFunction, int numberOfReplicas,
	                      Collection<T> nodes) {
		this.hashFunction = hashFunction;
		this.numberOfReplicas = numberOfReplicas;

		for (T node : nodes) {
			add(node);
		}
	}

	public void add(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			int hasCode = hashFunction.hash(node.hashString() + "-" + i);
			logger.debug("add Node:" + node.hashString() + "with hashCode:" + hasCode);
			circle.put(hasCode, node);
		}
	}

	public void remove(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.remove(hashFunction.hash(node.hashString() + "-" + i));
		}
	}

	public boolean contains(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			if (circle.containsKey(hashFunction.hash(node.hashString() + "-" + i))) {
				return true;
			}
		}
		return false;
	}

	public T get(Object key) {
		if (circle.isEmpty()) {
			return null;
		}
		int hash = hashFunction.hash(key);
		if (!circle.containsKey(hash)) {
			SortedMap<Integer, T> tailMap = circle.tailMap(hash);
			hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		return circle.get(hash);
	}

	public Collection<T> getAll() {
		return circle.values();
	}

	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	public void setNumberOfReplicas(int numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
	}

	public HashFunction getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(HashFunction hashFunction) {
		this.hashFunction = hashFunction;
	}

	public void clear() {
		circle.clear();
	}
}
