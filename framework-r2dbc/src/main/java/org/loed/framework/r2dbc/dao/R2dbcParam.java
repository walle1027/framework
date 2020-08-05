package org.loed.framework.r2dbc.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/3 7:29 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R2dbcParam {
	private Class<?> paramType;
	private Object paramValue;
}
