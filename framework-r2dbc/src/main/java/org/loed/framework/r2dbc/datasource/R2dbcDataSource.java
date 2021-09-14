package org.loed.framework.r2dbc.datasource;

import lombok.Data;
import org.loed.framework.common.balancer.Balanceable;

import java.time.Duration;

/**
 * @author thomason
 * @version 1.0
 * @since 2021/3/12 6:00 下午
 */
@Data
public class R2dbcDataSource implements Balanceable {
	/**
	 * Database name.
	 */
	private String name;

	/**
	 * R2DBC URL of the database. database name, username, password and pooling options
	 * specified in the url take precedence over individual options.
	 */
	private String url;

	/**
	 * Login username of the database. Set if no username is specified in the url.
	 */
	private String username;

	/**
	 * Login password of the database. Set if no password is specified in the url.
	 */
	private String password;

	/**
	 * Maximum amount of time that a connection is allowed to sit idle in the pool.
	 */
	private Duration maxIdleTime = Duration.ofMinutes(30);

	/**
	 * Maximum lifetime of a connection in the pool. By default, connections have an
	 * infinite lifetime.
	 */
	private Duration maxLifeTime;

	/**
	 * Maximum time to acquire a connection from the pool. By default, wait
	 * indefinitely.
	 */
	private Duration maxAcquireTime;

	/**
	 * Maximum time to wait to create a new connection. By default, wait indefinitely.
	 */
	private Duration maxCreateConnectionTime;

	/**
	 * Initial connection pool size.
	 */
	private int initialSize = 10;

	/**
	 * Maximal connection pool size.
	 */
	private int maxSize = 10;

	/**
	 * Validation query.
	 */
	private String validationQuery;

	/**
	 * weight
	 */
	private int weight;

	/**
	 * 数据库的唯一标识
	 *
	 * @return 唯一标识
	 */
	public String uniqueName() {
		return name + ":" + url;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public int getWeight() {
		return weight;
	}
}
