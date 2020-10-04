package org.loed.framework.mybatis.inspector.autoconfigure;

import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfo;
import lombok.Data;

import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/25 1:51 PM
 */
@Data
public class RoutingProperties {
	private Map<String , DatabaseMetaInfo> configs;
}
