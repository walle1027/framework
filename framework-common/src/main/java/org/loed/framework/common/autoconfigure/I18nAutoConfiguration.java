package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.i18n.DefaultI18nProvider;
import org.loed.framework.common.i18n.I18nProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/9/23 下午12:59
 */
@Configuration
public class I18nAutoConfiguration {

//	@Bean
//	@ConditionalOnMissingBean
//	@ConditionalOnClass(StringRedisTemplate.class)
//	@ConditionalOnBean(StringRedisTemplate.class)
//	public I18nProvider redisI18nProvider(StringRedisTemplate redisTemplate) {
//		RedisI18nProvider redisI18nProvider = new RedisI18nProvider();
//		redisI18nProvider.setRedisTemplate(redisTemplate);
//		return redisI18nProvider;
//	}

	@Bean
	@ConditionalOnMissingBean
	public I18nProvider defaultI18nProvider(){
		return new DefaultI18nProvider();
	}
}
