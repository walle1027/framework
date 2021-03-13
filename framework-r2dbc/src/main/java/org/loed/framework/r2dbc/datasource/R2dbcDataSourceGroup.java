package org.loed.framework.r2dbc.datasource;

import lombok.Data;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/3/12 5:56 下午
 */
@Data
public class R2dbcDataSourceGroup {
	private String name;
	private R2dbcDataSource master;
	private R2dbcDataSource[] slaves;
	private int weight;
}
