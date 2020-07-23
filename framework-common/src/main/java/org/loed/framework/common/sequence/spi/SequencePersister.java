package org.loed.framework.common.sequence.spi;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/21.20:42
 */
public interface SequencePersister {
	/**
	 * 获取下一个序列
	 *
	 * @param sequenceName 序列名称
	 * @param step         每次序列递增的步长
	 * @return 下一个序列
	 */
	Long getNext(String sequenceName, int step);

	void setOffset(String sequenceName, long offset);
}
