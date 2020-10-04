package org.loed.framework.mybatis.datasource.autoconfigure;

import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.meta.impl.DefaultDatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.meta.impl.ZKDatabaseMetaInfoProvider;
import org.loed.framework.mybatis.datasource.routing.AbstractRoutingDataSource;
import org.loed.framework.mybatis.datasource.routing.impl.C3p0RoutingDataSource;
import org.loed.framework.mybatis.datasource.routing.impl.DruidRoutingDataSource;
import org.loed.framework.mybatis.inspector.autoconfigure.DataSourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/14 下午6:24
 */
@Configuration
@ConditionalOnClass(AbstractRoutingDataSource.class)
@Import({ReadWriteAopAutoConfiguration.class})
@EnableConfigurationProperties(DataSourceProperties.class)
public class RoutingDataSourceConfigure {
	@Autowired
	private DataSourceProperties dataSourceProperties;

	@Bean
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "provider", havingValue = "default")
	public DatabaseMetaInfoProvider defaultMetaInfoProvider() {
		DefaultDatabaseMetaInfoProvider defaultMetaInfoProvider = new DefaultDatabaseMetaInfoProvider();
		defaultMetaInfoProvider.setName(dataSourceProperties.getDatabaseName());
		Map<String, DatabaseMetaInfo> configs = dataSourceProperties.getRouting().getConfigs();
		if (configs == null || configs.isEmpty()) {
			throw new IllegalArgumentException("empty routing configs for:" + ConfigureConstant.datasource_ns);
		}
		Map<String, DatabaseMetaInfo> newConfigs = new HashMap<>();
		for (Map.Entry<String, DatabaseMetaInfo> entry : configs.entrySet()) {
			String tenantId = entry.getKey();
			DatabaseMetaInfo databaseMetaInfo = entry.getValue();
			//TODO 处理
			databaseMetaInfo.setHorizontalShardingKey("tenantId");
			databaseMetaInfo.setHorizontalShardingValue(tenantId);
			databaseMetaInfo.setDatabase(dataSourceProperties.getDatabaseName());
			databaseMetaInfo.setStrategy(null);
			String key = defaultMetaInfoProvider.buildKey(tenantId, null);
			newConfigs.put(key, databaseMetaInfo);
		}
		dataSourceProperties.getRouting().setConfigs(newConfigs);
		return defaultMetaInfoProvider;
	}

	@Bean
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "provider", havingValue = "zookeeper")
	public DatabaseMetaInfoProvider zkMetaInfoProvider() {
		ZKDatabaseMetaInfoProvider zkDbMetaInfoProvider = new ZKDatabaseMetaInfoProvider();
		zkDbMetaInfoProvider.setDatabase(dataSourceProperties.getDatabaseName());
		zkDbMetaInfoProvider.setZkAddress(dataSourceProperties.getZkAddress());
		return zkDbMetaInfoProvider;
	}

	@Bean
	@ConditionalOnBean(DatabaseMetaInfoProvider.class)
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "type", matchIfMissing = true, havingValue = "c3p0")
	public DataSource c3p0RoutingDatasource(DatabaseMetaInfoProvider databaseMetaInfoProvider) {
		C3p0RoutingDataSource routingDataSource = new C3p0RoutingDataSource();
		routingDataSource.setReadWriteIsolate(dataSourceProperties.isReadWriteIsolate());
		routingDataSource.setHorizontalSharding(true);
		routingDataSource.setMetaInfoProvider(databaseMetaInfoProvider);
		return routingDataSource;
	}

	@Bean
	@ConditionalOnBean(DatabaseMetaInfoProvider.class)
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "type", havingValue = "druid")
	public DataSource druidRoutingDatasource(DatabaseMetaInfoProvider databaseMetaInfoProvider) {
		DruidRoutingDataSource routingDataSource = new DruidRoutingDataSource();
		routingDataSource.setReadWriteIsolate(dataSourceProperties.isReadWriteIsolate());
		routingDataSource.setHorizontalSharding(true);
		routingDataSource.setMetaInfoProvider(databaseMetaInfoProvider);
		routingDataSource.setMaxActive(dataSourceProperties.getDruid().getMaxActive());
		routingDataSource.setMinIdle(dataSourceProperties.getDruid().getMinIdle());
		routingDataSource.setInitialSize(dataSourceProperties.getDruid().getInitialSize());
		return routingDataSource;
	}
}
