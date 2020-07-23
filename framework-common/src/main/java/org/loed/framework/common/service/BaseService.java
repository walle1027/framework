package org.loed.framework.common.service;

import org.loed.framework.common.mapping.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.validation.ValidatorFactory;

/**
 * 所有服务类的父类
 * 提供一些基础方法
 *
 * @author Thomason
 * @version 1.0
 */
public class BaseService {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 数据验证器
	 */
	@Autowired(required = false)
	protected ValidatorFactory validatorFactory;
	/**
	 * 数据映射对象
	 */
	@Autowired
	protected Mapper mapper;

	/**
	 * 回滚事务，方便操作
	 */
	protected void rollback() {
		TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
	}

	public ValidatorFactory getValidatorFactory() {
		return validatorFactory;
	}

	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}

	public Mapper getMapper() {
		return mapper;
	}

	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}
}
