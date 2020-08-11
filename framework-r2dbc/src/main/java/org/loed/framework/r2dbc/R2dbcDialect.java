package org.loed.framework.r2dbc;

import lombok.extern.slf4j.Slf4j;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 10:24 上午
 */
@Slf4j
public enum R2dbcDialect {
	/**
	 * mysql
	 */
	mysql,
	/**
	 * postgres
	 */
	postgres,
	/**
	 * 未知数据库类型
	 */
	unknown;

	/**
	 * 从r2dbc的连接字符串 自动判断方言类型
	 *
	 * @param url 连接字符串
	 * @return 数据库类型
	 */
	public static R2dbcDialect autoGuessDialect(String url) {
		if (url.contains("r2dbc:")) {
			String substring = url.substring("r2dbc:".length());
			if (substring.contains(":")) {
				String dialect = substring.substring(0, substring.indexOf(":"));
				return R2dbcDialect.valueOf(dialect.trim().toLowerCase());
			} else {
				log.error("can't find dialect from url:" + url);
				return unknown;
			}
		} else {
			log.error("can't find dialect from url:" + url + ",not start with r2dbc");
			return unknown;
		}
	}
}
