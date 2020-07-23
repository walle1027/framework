package org.loed.framework.r2dbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.r2dbc.core.DatabaseClient;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/8 11:14 上午
 */
public class R2dbcDaoRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		R2dbcDaoClassPathScanner scanner = new R2dbcDaoClassPathScanner(registry);

		// this check is needed in Spring 3.1
		if (resourceLoader != null) {
			scanner.setResourceLoader(resourceLoader);
		}
		scanner.registerFilters();
		scanner.doScan("org.loed.framework.r2dbc");
	}
}
