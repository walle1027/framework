package org.loed.framework.mybatis.datasource.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.balancer.BalancerFactory;
import org.loed.framework.common.balancer.FocusBalancer;
import org.loed.framework.mybatis.datasource.meta.DataSourceGroup;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
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
import java.util.Arrays;
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
@Slf4j
public class RoutingDataSourceConfigure {
	@Autowired
	private DataSourceProperties dataSourceProperties;

	@Bean
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "provider", havingValue = "default")
	public DatabaseMetaInfoProvider defaultMetaInfoProvider() {
		Map<String, Map<String, DataSourceGroup>> configs = dataSourceProperties.getRouting().getConfigs();
		if (configs == null || configs.isEmpty()) {
			throw new IllegalArgumentException("empty routing configs for:" + ConfigureConstant.datasource_ns);
		}
		Map<String, FocusBalancer<DataSourceMetaInfo>> newConfigs = new HashMap<>();
		for (Map.Entry<String, Map<String, DataSourceGroup>> entry : configs.entrySet()) {
			String routingKey = entry.getKey();
			Map<String, DataSourceGroup> routingMap = entry.getValue();
			if (routingMap == null || routingMap.isEmpty()) {
				log.error("routing map is empty for routing key:{}", routingKey);
				continue;
			}
			for (Map.Entry<String, DataSourceGroup> routingMapEntry : routingMap.entrySet()) {
				String routingValue = routingMapEntry.getKey();
				DataSourceGroup dataSourceGroup = routingMapEntry.getValue();
				String uniqueKey = DefaultDatabaseMetaInfoProvider.uniqueKey(routingKey, routingValue);

				FocusBalancer<DataSourceMetaInfo> balancedDataSource = new FocusBalancer<>(dataSourceGroup.getMaster(), BalancerFactory.getBalancer(dataSourceProperties.getReadBalanceStrategy()));
				if (dataSourceGroup.getSlaves() != null && dataSourceGroup.getSlaves().length > 0) {
					balancedDataSource.updateProfiles(Arrays.asList(dataSourceGroup.getSlaves()));
				}
				newConfigs.put(uniqueKey, balancedDataSource);
			}
		}
		DefaultDatabaseMetaInfoProvider defaultMetaInfoProvider = new DefaultDatabaseMetaInfoProvider(newConfigs);
		defaultMetaInfoProvider.setName(dataSourceProperties.getDatabaseName());
		return defaultMetaInfoProvider;
	}

	@Bean
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "provider", havingValue = "zookeeper")
	public DatabaseMetaInfoProvider zkMetaInfoProvider() {
		ZKDatabaseMetaInfoProvider zkDbMetaInfoProvider = new ZKDatabaseMetaInfoProvider(dataSourceProperties.getReadBalanceStrategy(), dataSourceProperties.isAllowAutoAllocate());
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
		routingDataSource.setMetaInfoProvider(databaseMetaInfoProvider);
		return routingDataSource;
	}

	@Bean
	@ConditionalOnBean(DatabaseMetaInfoProvider.class)
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "type", havingValue = "druid")
	public DataSource druidRoutingDatasource(DatabaseMetaInfoProvider databaseMetaInfoProvider) {
		DruidRoutingDataSource routingDataSource = new DruidRoutingDataSource();
		routingDataSource.setReadWriteIsolate(dataSourceProperties.isReadWriteIsolate());
		routingDataSource.setMetaInfoProvider(databaseMetaInfoProvider);
		routingDataSource.setMaxActive(dataSourceProperties.getDruid().getMaxActive());
		routingDataSource.setMinIdle(dataSourceProperties.getDruid().getMinIdle());
		routingDataSource.setInitialSize(dataSourceProperties.getDruid().getInitialSize());
		return routingDataSource;
	}
}
