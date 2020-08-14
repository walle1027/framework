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
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/11/6 9:27 AM
 */
@RestControllerAdvice
public class DefaultExceptionHandler {
	@Autowired
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
		result.setCode(SystemConstant.MSG_ERROR);
		try {
			if (ex instanceof BusinessException) {
				List<Message> errors = ((BusinessException) ex).getErrors();
				int code = SystemConstant.MSG_ERROR;
				if (errors != null) {
					for (Message error : errors) {
						Object[] args = error.getArgs();
						if (args != null) {
							error.setText(i18nProvider.getText(error.getKey() + "", args));
						} else {
							error.setText(i18nProvider.getText(error.getKey() + ""));
						}
						code = error.getType();
					}
					result.setMessage(errors.stream().map(Message::getText).collect(Collectors.joining("\n")));
					result.setCode(code);
				}
			} else if (ex instanceof BindException) {
				BindingResult bindingResult = ((BindException) ex).getBindingResult();
				List<FieldError> fieldErrors = bindingResult.getFieldErrors();
				if (CollectionUtils.isNotEmpty(fieldErrors)) {
					String message = fieldErrors.stream().map(fieldError -> {
						String defaultMessage = fieldError.getDefaultMessage();
						return i18nProvider.getText(defaultMessage);
					}).collect(Collectors.joining("\n"));
					result.setMessage(message);
				}
			} else if (ex instanceof ConstraintViolationException) {
				Set<ConstraintViolation<?>> violationSet = ((ConstraintViolationException) ex).getConstraintViolations();
				if (CollectionUtils.isNotEmpty(violationSet)) {
					String message = violationSet.stream().map(v -> {
						return i18nProvider. getText(v.getMessage());
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
