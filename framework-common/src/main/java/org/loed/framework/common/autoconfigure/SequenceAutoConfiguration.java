package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.sequence.SequenceGenerator;
import org.loed.framework.common.sequence.spi.SequencePersister;
import org.loed.framework.common.sequence.suppport.JdbcSequencePersister;
import org.loed.framework.common.sequence.suppport.ZKSequenceSequencePersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/27 下午10:33
 */
@Configuration
@ConditionalOnClass(SequenceGenerator.class)
@AutoConfigureAfter(DataSource.class)
@EnableConfigurationProperties({CommonProperties.class})
public class SequenceAutoConfiguration {
	@Autowired
	private CommonProperties commonProperties;

	@Bean
	@Conditional(ZKExistsCondition.class)
	public ZKSequenceSequencePersister zkSequenceSequencePersister(@Autowired(required = false) ZKDistributeLock distributeLock) {
		String zkAddress = commonProperties.getZkAddress();
		ZKSequenceSequencePersister zkSequenceSequencePersister = new ZKSequenceSequencePersister(zkAddress);
		if (distributeLock != null) {
			zkSequenceSequencePersister.setDistributeLock(distributeLock);
		}
		return zkSequenceSequencePersister;
	}

	@Bean
	@ConditionalOnMissingBean({ZKSequenceSequencePersister.class})
	@ConditionalOnBean(DataSource.class)
	public JdbcSequencePersister jdbcSequencePersister(DataSource dataSource) {
		return new JdbcSequencePersister(dataSource);
	}

	@Bean
	@ConditionalOnBean(SequencePersister.class)
	public SequenceGenerator sequenceGenerator(SequencePersister sequencePersister) {
		return new SequenceGenerator(sequencePersister);
	}
}
