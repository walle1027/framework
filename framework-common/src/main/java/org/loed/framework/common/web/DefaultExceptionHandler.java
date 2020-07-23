package org.loed.framework.common.web;

import org.loed.framework.common.Result;
import org.loed.framework.common.i18n.I18nProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/11/6 9:27 AM
 */
@RestControllerAdvice
public class DefaultExceptionHandler {
	@Autowired
	private I18nProvider i18nProvider;

	private Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@ExceptionHandler
	public Result<Void> handleException(HttpServletRequest request, Throwable e) {
		//先打印异常
		logger.error("request:" + request.getRequestURI() + " occurs error => " + e.getMessage(), e);
		//再返回包装后的结果
		return i18nProvider.resolveException(e);
	}
}
