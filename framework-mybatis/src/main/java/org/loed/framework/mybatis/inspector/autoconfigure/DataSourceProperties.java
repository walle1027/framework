package org.loed.framework.mybatis.inspector.autoconfigure;

import lombok.Data;
import org.loed.framework.common.ConfigureConstant;
import org.loed.framework.common.balancer.BalanceStrategy;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.mybatis.datasource.meta.DataSourceMetaInfo;
import org.loed.framework.mybatis.datasource.meta.DruidProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/15 下午1:51
 */
@ConfigurationProperties(prefix = ConfigureConstant.datasource_ns)
@Data
public class DataSourceProperties {
	/**
	 * 数据库逻辑名称，用于区分不同的应用
	 */
	private String databaseName;
	/**
	 * 路由信息提供方式 zookeeper zk中配置
	 */
	private DataSourceMetadataProvider provider = DataSourceMetadataProvider.zookeeper;
	/**
	 * 真实数据源类型 c3p0 druid
	 */
	private DataSourceType type = DataSourceType.druid;
	/**
	 * druid配置属性
	 */
	private DruidProperties druid = new DruidProperties();
	/**
	 * 是否做读写分离 默认
	 */
	private boolean readWriteIsolate = true;
	/**
	 * 是否以tenantCode做路由key
	 */
	private String routingProps = SystemContext.CONTEXT_TENANT_ID;
	/**
	 * zk地址
	 */
	private String zkAddress;
	/**
	 * batchSize
	 */
	private int batchSize;

	/**
	 * 是否自动分配数据源
	 */
	private boolean allowAutoAllocate = true;
	/**
	 * 路由配置信息
	 */
	private RoutingProperties routing;

	/**
	 * 如果读写数据源是配置的，写库的配置项
	 */
	private DataSourceMetaInfo write;
	/**
	 * 如果读写数据源是配置的，读库的配置项
	 */
	private List<DataSourceMetaInfo> reads;
	/**
	 * 读库负责均衡策略
	 */
	private BalanceStrategy readBalanceStrategy = BalanceStrategy.roundRobin;

	public enum DataSourceMetadataProvider {
		zookeeper, config
	}
}
