package org.loed.framework.r2dbc.autoconfigure;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/9 1:23 下午
 */
public class R2dbcDbInspectorRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private Environment environment;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {
		AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(R2dbcDbInspector.class.getName()));
		if (annoAttrs == null) {
			return;
		}
		Binder binder = Binder.get(environment);
		BindResult<R2dbcProperties> bindResult = binder.bind("org.loed.framework.r2dbc", R2dbcProperties.class);
		R2dbcProperties properties = bindResult.orElseGet(R2dbcProperties::new);
		List<String> basePackages = new ArrayList<String>();
		for (String pkg : annoAttrs.getStringArray("value")) {
			if (StringUtils.isNotBlank(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : annoAttrs.getStringArray("basePackages")) {
			if (StringUtils.isNotBlank(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		beanDefinition.setNonPublicAccessAllowed(true);
		beanDefinition.setBeanClass(org.loed.framework.r2dbc.inspector.R2dbcDbInspector.class);
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanReference(ConnectionFactory.class));
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, new RuntimeBeanReference(DatabaseDialect.class));

		beanDefinition.getPropertyValues().addPropertyValue("packagesToScan", basePackages);
		beanDefinition.getPropertyValues().addPropertyValue("enabled", properties.getInspector().isEnabled());
		beanDefinition.getPropertyValues().addPropertyValue("execute", properties.getInspector().isExecute());
		registry.registerBeanDefinition("r2dbcDbInspector", beanDefinition);
	}

	@Override
	public void setEnvironment(@NonNull Environment environment) {
		this.environment = environment;
	}
}
