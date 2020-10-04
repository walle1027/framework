package org.loed.framework.mybatis.autoconfigure;

import com.github.pagehelper.PageInterceptor;
import org.loed.framework.mybatis.interceptor.ChainedInterceptor;
import org.loed.framework.mybatis.interceptor.NamedWrapperInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Properties;

/**
 * 自定注入分页插件
 *
 * @author liuzh
 */
@Configuration
@ConditionalOnClass(SqlSessionFactory.class)
@PropertySource("classpath:page-helper.properties")
@EnableConfigurationProperties(PageHelperProperties.class)
public class PageHelperAutoConfiguration implements EnvironmentAware {

	@Autowired
	private PageHelperProperties pageHelperProperties;

	@Resource
	private ChainedInterceptor chainedInterceptor;

	@Override
	public void setEnvironment(Environment environment) {
		Binder binder = Binder.get(environment);
		pageHelperProperties = binder.bind("pagehelper", PageHelperProperties.class).get();
	}

	@PostConstruct
	public void addPageInterceptor() {
		PageInterceptor interceptor = new PageInterceptor();
		Properties properties = pageHelperProperties.getProperties();
		interceptor.setProperties(properties);
		chainedInterceptor.addInterceptor(ChainedInterceptor.PAGE_HELPER_ORDER, new NamedWrapperInterceptor("page-helper", interceptor));
	}

}
