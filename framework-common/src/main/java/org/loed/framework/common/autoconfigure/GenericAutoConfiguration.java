package org.loed.framework.common.autoconfigure;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.PoolExecutors;
import org.loed.framework.common.ServiceLocator;
import org.loed.framework.common.SpringUtils;
import org.loed.framework.common.ThreadPoolExecutor;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.mapping.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/12 下午3:12
 */
@Configuration
@EnableConfigurationProperties({CommonProperties.class})
public class GenericAutoConfiguration {
	private Logger logger = LoggerFactory.getLogger(GenericAutoConfiguration.class);
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private CommonProperties commonProperties;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	@Bean
	public MapperFactoryBean createMapper() {
		MapperFactoryBean mapperFactoryBean = new MapperFactoryBean();
		String mapperConfigLocations = commonProperties.getMapperConfigLocations();
		if (StringUtils.isNotBlank(mapperConfigLocations)) {
			List<Resource> resources = new ArrayList<>();
			Arrays.stream(mapperConfigLocations.split(",")).forEach(configFile -> {
				try {
					Resource[] resolverResources = resourcePatternResolver.getResources(configFile);
					Collections.addAll(resources, resolverResources);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			});
			mapperFactoryBean.setConfigFiles(resources.toArray(new Resource[resources.size()]));
		}
		return mapperFactoryBean;
	}

	@Bean
	@ConditionalOnMissingBean
	public ThreadPoolExecutor defaultThreadPool() {
		ThreadPoolProperties threadPoolProperties = commonProperties.getThreadPool();
		int coreSize = threadPoolProperties.getCoreSize();
		int maxSize = threadPoolProperties.getMaxSize();
		int queueCapacity = threadPoolProperties.getQueueCapacity();
		int keepAliveSeconds = threadPoolProperties.getKeepAliveSeconds();
		return PoolExecutors.newThreadPool("defaultThreadPool", coreSize, maxSize, queueCapacity, keepAliveSeconds);
	}

	@Bean
	@Conditional(ZKExistsCondition.class)
	public ZKDistributeLock distributeLock() {
		return new ZKDistributeLock(commonProperties.getZkAddress());
	}

	@PostConstruct
	public void setApplicationContext() {
		ServiceLocator.setApplicationContext(applicationContext);
		SpringUtils.setApplicationContext(applicationContext);
	}
}
