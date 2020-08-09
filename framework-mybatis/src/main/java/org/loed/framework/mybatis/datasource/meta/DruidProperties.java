package org.loed.framework.mybatis.datasource.meta;

import lombok.Data;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/15 下午4:11
 */
@Data
public class DruidProperties {
	private int initialSize = 10;
	private int maxActive = 200;
	private int minIdle = 10;
	private int maxIdle = 100;
	private long maxWait = -1;
	private int notFullTimeoutRetryCount = 0;
	private String validationQuery = "select 1";
	private int validationQueryTimeout = -1;
	private boolean testOnBorrow = false;
	private boolean testOnReturn = false;
	private boolean testWhileIdle = true;
	private boolean poolPreparedStatements = false;
	private boolean sharePreparedStatements = false;
	private int maxPoolPreparedStatementPerConnectionSize = 10;
	private long timeBetweenEvictionRunsMillis = 15 * 60 * 1000L;
}
