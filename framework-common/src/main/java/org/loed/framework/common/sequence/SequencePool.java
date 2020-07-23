package org.loed.framework.common.sequence;

import org.loed.framework.common.sequence.spi.SequencePersister;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-28 下午2:42
 */

public class SequencePool implements Serializable {
	//对象名称
	private final String sequenceName;
	//序列池大小
	private final int poolSize;
	//序列dao
	private final SequencePersister sequencePersister;
	//序列数组
	private volatile long offset;
	private AtomicBoolean resetting = new AtomicBoolean(false);
	//指针
	private int pointer = 0;

	/**
	 * 构造方法
	 *
	 * @param sequencePersister 持久化同步器
	 * @param sequenceName      对象名称
	 * @param poolSize          缓冲池大小
	 */
	SequencePool(SequencePersister sequencePersister, String sequenceName, int poolSize) {
		this.sequencePersister = sequencePersister;
		this.sequenceName = sequenceName;
		this.poolSize = poolSize;
		reset();
	}

	private void reset() {
		//构造新的id序列
		offset = sequencePersister.getNext(sequenceName, poolSize);
	}

	public void clear() {
		//删除数据库
		sequencePersister.setOffset(sequenceName, 0);
		//删除序列
		pointer = 0;
	}

	/**
	 * 取得下一个序列
	 *
	 * @return 下一个序列
	 */
	long getNext() {
		synchronized (this) {
			if (pointer >= poolSize) {
				resetting.set(true);
				reset();
				resetting.set(false);
			}
			while (resetting.get()) {
				Thread.yield();
			}
			return offset + pointer++;
		}
	}

	/**
	 * 取得下一个序列
	 *
	 * @return 下一个序列
	 */
	void setOffset(long offset) {
		sequencePersister.setOffset(sequenceName, offset);
	}
}
