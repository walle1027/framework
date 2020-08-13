package org.loed.framework.common.autoconfigure;

import org.loed.framework.common.ConfigureConstant;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/12 3:41 下午
 */
public class ResponseBodyWrapperCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
		Environment environment = conditionContext.getEnvironment();
		CommonProperties properties = Binder.get(environment).bind(ConfigureConstant.default_ns, CommonProperties.class).orElse(new CommonProperties());
		return properties.isAutoWrapResponse();
	}
}
