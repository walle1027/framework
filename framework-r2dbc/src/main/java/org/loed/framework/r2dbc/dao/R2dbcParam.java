package org.loed.framework.r2dbc.dao;

import lombok.Data;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/3 7:29 下午
 */
@Data
public class R2dbcParam {
	private Class<?> paramType;
	private Object paramValue;

	public R2dbcParam() {
	}

	public R2dbcParam(Class<?> paramType, Object paramValue) {
//		if (paramType.isEnum()) {
//			this.paramType = String.class;
//		} else {
			this.paramType = paramType;
//		}
		this.paramValue = paramValue;
	}
}
