package org.loed.framework.mybatis.datasource.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.autoconfigure.CommonProperties;
import org.loed.framework.common.autoconfigure.ZKExistsCondition;
import org.loed.framework.common.balancer.BalanceStrategy;
import org.loed.framework.common.balancer.Balancer;
import org.loed.framework.common.balancer.BalancerFactory;
import org.loed.framework.common.zookeeper.ZKHolder;
import org.loed.framework.mybatis.inspector.autoconfigure.DataSourceProperties;
import org.loed.framework.mybatis.datasource.creator.DataSourceCreator;
import org.loed.framework.mybatis.datasource.creator.druid.DruidDataSourceCreator;
import org.loed.framework.mybatis.datasource.creator.druid.DruidDataSourceDecorator;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteIsolateDataSource;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.ReadWriteDataSourceProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.support.ConfiguredReadWriteDataSourceProvider;
import org.loed.framework.mybatis.datasource.readwriteisolate.provider.support.ZKReadWriteDataSourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/6/20 3:24 PM
 */
@Configuration
public class ReadWriteDataSourceConfiguration {
	@Autowired
	private DataSourceProperties properties;
	@Autowired
	private CommonProperties commonProperties;

	@Bean
	@ConditionalOnClass(DruidDataSource.class)
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "type", havingValue = "druid", matchIfMissing = true)
	public DataSourceCreator dataSourceCreator() {
		DruidDataSourceCreator druidDataSourceCreator = new DruidDataSourceCreator(properties.getDruid());
		return new DruidDataSourceDecorator(druidDataSourceCreator);
	}

	@Bean
	@Conditional(ZKExistsCondition.class)
	@ConditionalOnMissingBean
	public ReadWriteDataSourceProvider zkReadWriteDataSourceProvider() {
		String zkAddress = commonProperties.getZkAddress();
		BalanceStrategy readBalanceStrategy = properties.getReadBalanceStrategy();
		Balancer<DataSourceMetaInfo> balancer = BalancerFactory.getBalancer(readBalanceStrategy);
		return new ZKReadWriteDataSourceProvider(properties.getDatabaseName(), ZKHolder.get(zkAddress), balancer);
	}

	@Bean
	@ConditionalOnMissingBean
	public ReadWriteDataSourceProvider configurableReadWriteDataSourceProvider() {
		//check the configuration
		DataSourceMetaInfo write = properties.getWrite();
		List<DataSourceMetaInfo> reads = properties.getReads();
		if (write == null) {
			throw new RuntimeException("configured write datasource is null");
		}

		if (reads == null || reads.size() == 0) {
			throw new RuntimeException("configured read datasource is null or empty");
		}

		BalanceStrategy readBalanceStrategy = properties.getReadBalanceStrategy();

		Balancer<DataSourceMetaInfo> balancer = BalancerFactory.getBalancer(readBalanceStrategy);

		ConfiguredReadWriteDataSourceProvider dataSourceProvider = new ConfiguredReadWriteDataSourceProvider(balancer);
		if (write.getDatabaseName() == null) {
			write.setDatabaseName(properties.getDatabaseName());
		}
		dataSourceProvider.setWriteDataSource(write);

		reads.forEach(read -> {
			if (read.getDatabaseName() == null) {
				read.setDatabaseName(properties.getDatabaseName());
			}
		});

		dataSourceProvider.setReadDataSource(reads);

		return dataSourceProvider;
	}

	@Bean
	@ConditionalOnMissingClass(value = "org.loed.framework.mybatis.datasource.autoconfigure.RoutingDataSourceConfigure")
	@ConditionalOnProperty(prefix = ConfigureConstant.datasource_ns, name = "readWriteIsolate", havingValue = "true", matchIfMissing = true)
	public ReadWriteIsolateDataSource readWriteIsolateDataSource(DataSourceCreator dataSourceCreator, ReadWriteDataSourceProvider readWriteDataSourceProvider) {
		return new ReadWriteIsolateDataSource(readWriteDataSourceProvider, dataSourceCreator);
	}
}
