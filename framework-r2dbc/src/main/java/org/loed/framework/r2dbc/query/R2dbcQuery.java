package org.loed.framework.r2dbc.query;

import lombok.Data;

import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/3 7:29 下午
 */
@Data
public class R2dbcQuery {
	private final String statement;
	private final Map<String, R2dbcParam> params;

	public R2dbcQuery(String statement, Map<String, R2dbcParam> params) {
		this.statement = statement;
		this.params = params;
	}
}
