package org.loed.framework.common.sequence;

import org.loed.framework.common.sequence.spi.SequencePersister;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-28 下午2:42
 */

public class SequenceGenerator {
	private static Lock getLock = new ReentrantLock();
	private static Lock setLock = new ReentrantLock();
	private final ConcurrentHashMap<String, SequencePool> SEQUENCE_POOL = new ConcurrentHashMap<String, SequencePool>(12);
	private final int DEFAULT_POOL_SIZE = 10000;
	private final SequencePersister sequencePersister;

	public SequenceGenerator(SequencePersister sequencePersister) {
		this.sequencePersister = sequencePersister;
	}

	/**
	 * 取得下一个主键
	 *
	 * @param sequenceName 目标名称
	 * @return 主键
	 */
	public void setOffset(String sequenceName) {
		setOffset(sequenceName, 0);
	}


	/**
	 * 取得下一个主键
	 *
	 * @param sequenceName 目标名称
	 * @return 主键
	 */
	public void setOffset(String sequenceName, long offset) {
		if (SEQUENCE_POOL.get(sequenceName) == null) {
			try {
				setLock.lock();
				if (SEQUENCE_POOL.get(sequenceName) == null) {
					SequencePool sequencePool = new SequencePool(sequencePersister, sequenceName, 1);
					SEQUENCE_POOL.put(sequenceName, sequencePool);
				}
			} finally {
				setLock.unlock();
			}
		}

		SequencePool sequencePool = SEQUENCE_POOL.get(sequenceName);
		sequencePool.setOffset(offset);
	}

	/**
	 * 取得下一个主键
	 *
	 * @param sequenceName 目标名称
	 * @return 主键
	 */
	public long getNext(String sequenceName) {
		return getNext(sequenceName, DEFAULT_POOL_SIZE);
	}

	/**
	 * 取得下一个序列
	 *
	 * @param sequenceName 对象名称
	 * @param poolSize     缓冲池大小
	 * @return 下一个序列
	 * @see SequencePool#getNext
	 */
	public long getNext(String sequenceName, int poolSize) {
		if (SEQUENCE_POOL.get(sequenceName) == null) {
			try {
				getLock.lock();
				if (SEQUENCE_POOL.get(sequenceName) == null) {
					SequencePool sequencePool = new SequencePool(sequencePersister, sequenceName, poolSize);
					SEQUENCE_POOL.put(sequenceName, sequencePool);
				}
			} finally {
				getLock.unlock();
			}
		}
		return SEQUENCE_POOL.get(sequenceName).getNext();
	}

	/**
	 * 清空序列
	 *
	 * @param sequenceName 对象名称
	 */
	public void clear(String sequenceName) {
		if (SEQUENCE_POOL.get(sequenceName) != null) {
			SequencePool sequencePool = SEQUENCE_POOL.get(sequenceName);
			sequencePool.clear();
		}
	}
}
