package org.loed.framework.r2dbc.dao;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/3 7:29 下午
 */
@Data
public class R2dbcQuery {
	private String statement;
	private Map<String, R2dbcParam> params;
}
