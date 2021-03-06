package org.loed.framework.mybatis.datasource.autoconfigure;


import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteAop;
import org.loed.framework.mybatis.datasource.readwriteisolate.ReadWriteIsolation;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

@Configuration
@AutoConfigureAfter(TransactionAttributeSourceAdvisor.class)
public class ReadWriteAopAutoConfiguration {

	@Autowired(required = false)
	private BeanFactoryTransactionAttributeSourceAdvisor transactionAttributeSourceAdvisor;

	@Bean
	public Advice advice() {
		return new ReadWriteAop();
	}

	@Bean
	public Advisor advisor() {
		AspectJExpressionPointcutAdvisor pointcutAdvisor = new AspectJExpressionPointcutAdvisor();
		pointcutAdvisor.setAdvice(advice());
		String expression = "@within(" + ReadWriteIsolation.class.getName() + ") || @annotation(" + ReadWriteIsolation.class.getName() + ")";
		if (transactionAttributeSourceAdvisor != null) {
			pointcutAdvisor.setOrder(transactionAttributeSourceAdvisor.getOrder() - 1);
		} else {
			pointcutAdvisor.setOrder(Ordered.LOWEST_PRECEDENCE);
		}
		pointcutAdvisor.setExpression(expression);
		return pointcutAdvisor;
	}
}
