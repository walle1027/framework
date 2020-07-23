package org.loed.framework.common.autoconfigure;

import lombok.Data;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/1/24 下午3:29
 */
@Data
public class ThreadPoolProperties {
	private int coreSize = 50;
	private int maxSize = 300;
	private int queueCapacity = Integer.MAX_VALUE;
	private int keepAliveSeconds = 300;
}
