package org.loed.framework.common.web.flux;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.loed.framework.common.BusinessException;
import org.loed.framework.common.Message;
import org.loed.framework.common.Result;
import org.loed.framework.common.SystemConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.NestedServletException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/13 4:01 下午
 */
@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {
	@Autowired(required = false)
	private ReactiveI18nProvider i18nProvider;

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.OK)
	public Mono<Result<Void>> handleException(Exception ex, ServerWebExchange exchange) {
		String value = exchange.getRequest().getURI().toString();
		log.error(value + " occurs exception caused by :" + ex.getMessage(), ex);
		exchange.getAttributes().put(SystemConstant.RESPONSE_WRAPPED, true);
		return resolveException(ex);
	}

	/**
	 * 解析异常类
	 *
	 * @param ex 异常类
	 * @return 包装后的结果
	 */
	private Mono<Result<Void>> resolveException(Throwable ex) {
		if (i18nProvider == null) {
			i18nProvider = ReactiveI18nProvider.DEFAULT_REACTIVE_I18N_PROVIDER;
		}
		if (ex instanceof BusinessException) {
			List<Message> businessErrors = ((BusinessException) ex).getErrors();
			if (businessErrors != null) {
				return Flux.fromIterable(businessErrors).flatMap(error -> {
					Object[] args = error.getArgs();
					if (args != null) {
						return i18nProvider.getText(error.getKey() + "", args);
					} else {
						return i18nProvider.getText(error.getKey() + "");
					}
				}).collectList().map(this::convertToResult);
			} else {
				return Mono.just(Result.UNKNOWN_ERROR);
			}
		} else if (ex instanceof BindException) {
			BindingResult bindingResult = ((BindException) ex).getBindingResult();
			List<FieldError> fieldErrors = bindingResult.getFieldErrors();
			if (CollectionUtils.isNotEmpty(fieldErrors)) {
				return Flux.fromIterable(fieldErrors).flatMap(fieldError -> {
					String defaultMessage = fieldError.getDefaultMessage();
					return i18nProvider.getText(defaultMessage);
				}).collectList().map(this::convertToResult);
			} else {
				return Mono.just(Result.UNKNOWN_ERROR);
			}
		} else if (ex instanceof ConstraintViolationException) {
			Set<ConstraintViolation<?>> violationSet = ((ConstraintViolationException) ex).getConstraintViolations();
			if (CollectionUtils.isNotEmpty(violationSet)) {
				return Flux.fromIterable(violationSet).flatMap(error -> {
					return i18nProvider.getText(error.getMessage());
				}).collectList().map(this::convertToResult);
			} else {
				return Mono.just(Result.UNKNOWN_ERROR);
			}
		} else if (ex instanceof NestedServletException) {
			Throwable cause = ex.getCause();
			return resolveException(cause);
		} else {
			//这里是未知异常，直接报服务器错误
			Result<Void> result = new Result<>();
			result.setCode(SystemConstant.MSG_ERROR);
			String stackTrace = ExceptionUtils.getStackTrace(ex);
			if (stackTrace.length() > 300) {
				result.setMessage(stackTrace.substring(0, 300));
			} else {
				result.setMessage(stackTrace);
			}
			return Mono.just(result);
		}
	}

	private Result<Void> convertToResult(List<String> errors) {
		Result<Void> result = new Result<>();
		result.setCode(SystemConstant.MSG_ERROR);
		result.setMessage(String.join("\n", errors));
		return result;
	}
}
