package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.r2dbc.dao.R2dbcDao;
import org.loed.framework.r2dbc.dao.R2dbcDaoFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.r2dbc.core.DatabaseClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/8 11:17 上午
 */
public class R2dbcDaoClassPathScanner extends ClassPathBeanDefinitionScanner {

	public R2dbcDaoClassPathScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	public void registerFilters() {
		addExcludeFilter(new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
				return !metadataReader.getClassMetadata().isInterface();
			}
		});
		addIncludeFilter(new AssignableTypeFilter(R2dbcDao.class) {
			@Override
			protected Boolean matchSuperClass(String superClassName) {
				System.out.println(superClassName);
				return false;
			}

			@Override
			protected Boolean matchInterface(String interfaceName) {
				return super.matchInterface(interfaceName);
			}

			@Override
			protected boolean matchSelf(MetadataReader metadataReader) {
				return false;
			}

			@Override
			protected boolean matchClassName(String className) {
				return false;
			}
		});
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return true;
	}

	/**
	 * Calls the parent search that will search and register all the candidates.
	 * Then the registered objects are post processed to set them as
	 * MapperFactoryBeans
	 */
	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

		if (beanDefinitions.isEmpty()) {
			logger.warn("No dao interface found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
		} else {
			processBeanDefinitions(beanDefinitions);
		}
		return beanDefinitions;
	}

	private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
		GenericBeanDefinition definition;
		for (BeanDefinitionHolder holder : beanDefinitions) {
			definition = (GenericBeanDefinition) holder.getBeanDefinition();
			if (logger.isDebugEnabled()) {
				logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName()
						+ "' and '" + definition.getBeanClassName() + "' mapperInterface");
			}
			String beanClassName = holder.getBeanDefinition().getBeanClassName();
			if (StringUtils.isBlank(beanClassName)) {
				return;
			}
			definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
			definition.setScope(BeanDefinition.SCOPE_SINGLETON);
			// the mapper interface is the original class of the bean
			// but, the actual class of the bean is MapperFactoryBean
			definition.setLazyInit(true);
			definition.getConstructorArgumentValues().addIndexedArgumentValue(0, beanClassName);
			definition.getPropertyValues().add("databaseClient", new RuntimeBeanReference(DatabaseClient.class));
			definition.getPropertyValues().add("connectionFactory", new RuntimeBeanReference(ConnectionFactory.class));
			definition.getPropertyValues().add("properties", new RuntimeBeanReference(R2dbcProperties.class));
			definition.setBeanClass(R2dbcDaoFactoryBean.class);
		}
	}
}
