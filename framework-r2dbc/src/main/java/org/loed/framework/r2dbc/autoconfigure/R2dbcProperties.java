package org.loed.framework.r2dbc.autoconfigure;

import lombok.Data;
import org.loed.framework.r2dbc.R2dbcDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 3:44 下午
 */
@ConfigurationProperties(prefix = R2dbcProperties.PREFIX)
@Data
public class R2dbcProperties {

	public static final String PREFIX = "org.loed.framework.r2dbc";

	private int batchSize = 100;

	private boolean quote = true;

	private boolean enableRouting = false;

	private R2dbcDialect dialect = R2dbcDialect.mysql;

	private RoutingProvider routingProvider = RoutingProvider.zk;

	private String zkAddress;

	private Map<String, org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties> routing;

	private Inspector inspector = new Inspector();

	private Pool pool = new Pool();

	@Data
	public static class Pool {
		private Duration maxAcquireTime = Duration.ofSeconds(3);
		private Duration maxCreateConnectionTime = Duration.ofSeconds(5);
		private int maxRetry = 1;
	}

	@Data
	public static class Inspector {
		private boolean enabled = true;
		private boolean execute = true;
	}

	public enum RoutingProvider {
		/**
		 * 基于zk的路由配置
		 */
		zk,
		/**
		 * 基于配置文件的路由配置
		 */
		config
	}
}
