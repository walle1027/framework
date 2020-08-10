package org.loed.framework.r2dbc.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 3:44 下午
 */
@ConfigurationProperties(prefix = "org.loed.framework.r2dbc")
@Data
public class R2dbcProperties {

	private boolean quote = true;

	private Inspector inspector = new Inspector();

	@Data
	public static class Inspector {
		private boolean enabled = true;
		private boolean execute = true;
	}

}
