package org.loed.framework.common.web.mvc;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.loed.framework.common.BusinessException;
import org.loed.framework.common.Message;
import org.loed.framework.common.Result;
import org.loed.framework.common.SystemConstant;
import org.loed.framework.common.web.mvc.i18n.I18nProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/11/6 9:27 AM
 */
@RestControllerAdvice
public class DefaultExceptionHandler {
	@Autowired(required = false)
	private I18nProvider i18nProvider;

	private final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@ExceptionHandler
	public Result<Void> handleException(HttpServletRequest request, Throwable e) {
		//先打印异常
		logger.error("request:" + request.getRequestURI() + " occurs error => " + e.getMessage(), e);
		//再返回包装后的结果
		return resolveException(e);
	}

	/**
	 * 解析异常消息
	 *
	 * @param ex 异常类
	 * @return 返回结果
	 */
	private Result<Void> resolveException(Throwable ex) {
		Result<Void> result = new Result<>();
		result.setStatus(SystemConstant.SERVER_ERROR);
		try {
			if (i18nProvider == null) {
				i18nProvider = I18nProvider.DEFAULT_I18N_PROVIDER;
			}
			if (ex instanceof BusinessException) {
				Message error = ((BusinessException) ex).getMessageInfo();
				if (error != null) {
					Object[] args = error.getArgs();
					if (args != null) {
						error.setText(i18nProvider.getText(error.getI18nKey(), args));
					} else {
						error.setText(i18nProvider.getText(error.getI18nKey()));
					}
					result.setMessage(error.getText());
					result.setStatus(((BusinessException) ex).getErrorCode());
				}
			} else if (ex instanceof BindException || ex instanceof WebExchangeBindException) {
				BindingResult bindingResult = null;
				if (ex instanceof BindException) {
					bindingResult = ((BindException) ex).getBindingResult();
				} else {
					bindingResult = ((WebExchangeBindException) ex).getBindingResult();
				}
				List<FieldError> fieldErrors = bindingResult.getFieldErrors();
				if (CollectionUtils.isNotEmpty(fieldErrors)) {
					String message = fieldErrors.stream().map(fieldError -> {
						String defaultMessage = fieldError.getDefaultMessage();
						return i18nProvider.getText(defaultMessage);
					}).collect(Collectors.joining("\n"));
					result.setMessage(message);
				}
			} else if (ex instanceof NestedServletException) {
				Throwable cause = ex.getCause();
				return resolveException(cause);
			} else {
				//这里是未知异常，直接报服务器错误
				String stackTrace = ExceptionUtils.getStackTrace(ex);
				if (stackTrace.length() > 300) {
					result.setMessage(stackTrace.substring(0, 300));
				} else {
					result.setMessage(stackTrace);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			//此处已经获取异常信息失败了，直接返回错误消息
			String stackTrace = ExceptionUtils.getStackTrace(e);
			if (stackTrace.length() > 300) {
				result.setMessage(stackTrace.substring(0, 300));
			} else {
				result.setMessage(stackTrace);
			}
		}
		return result;
	}
}
