package org.loed.framework.common.autoconfigure;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.ConfigureConstant;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.NoSuchElementException;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/27 下午10:44
 */
public class ZKExistsCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Environment environment = context.getEnvironment();
		Binder binder = Binder.get(environment);
		try {
			CommonProperties commonProperties = binder.bind(ConfigureConstant.default_ns, CommonProperties.class).get();
			return commonProperties != null && StringUtils.isNotBlank(commonProperties.getZkAddress());
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
