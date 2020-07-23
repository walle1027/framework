package org.loed.framework.common.autoconfigure;

import lombok.Data;
import org.loed.framework.common.ConfigureConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/27 下午10:36
 */
@ConfigurationProperties(prefix = ConfigureConstant.default_ns)
@Data
public class CommonProperties {
	private String zkAddress;
	private String mapperConfigLocations = "classpath*:/mapper-configs/**/*.xml";
	private String systemContextFilterPattern = "*";
	private ThreadPoolProperties threadPool = new ThreadPoolProperties();
}
