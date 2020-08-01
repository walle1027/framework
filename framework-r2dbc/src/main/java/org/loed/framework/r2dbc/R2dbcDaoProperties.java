package org.loed.framework.r2dbc;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/31 6:30 下午
 */
@Data
public class R2dbcDaoProperties {
	public static final String Prefix = "org.loed.framework.r2dbc";
	private int batchSize;
}
