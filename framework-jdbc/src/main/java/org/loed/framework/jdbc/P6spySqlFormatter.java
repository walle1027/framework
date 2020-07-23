package org.loed.framework.jdbc;

import com.p6spy.engine.common.P6Util;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * 格式化输出sql
 *
 * @author wwj
 * @version 1.0.0
 */
public class P6spySqlFormatter implements MessageFormattingStrategy {
	public P6spySqlFormatter() {
	}

	@Override
	public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
		return now + " | " + P6Util.singleLine(sql);
	}
}
