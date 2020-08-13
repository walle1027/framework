package org.loed.framework.common.i18n;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.loed.framework.common.BusinessException;
import org.loed.framework.common.Message;
import org.loed.framework.common.Result;
import org.loed.framework.common.SystemConstant;
import org.loed.framework.common.context.SystemContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.util.NestedServletException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-11-2 下午11:56
 */

public interface I18nProvider {
	Logger logger = LoggerFactory.getLogger(I18nProvider.class);
	ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>();
	/**
	 * 默认的区域
	 */
	String DEFAULT_LOCALE = "zh_CN";
	/**
	 * i18nkey分隔符
	 */
	String I18N_KEY_SEPARATOR = ":";

	/**
	 * 解析异常消息
	 *
	 * @param ex 异常类
	 * @return 返回结果
	 */
	default Result<Void> resolveException(Throwable ex) {
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
							error.setText(this.getText(error.getKey() + "", args));
						} else {
							error.setText(this.getText(error.getKey() + ""));
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
						return this.getText(defaultMessage);
					}).collect(Collectors.joining("\n"));
					result.setMessage(message);
				}
			} else if (ex instanceof ConstraintViolationException) {
				Set<ConstraintViolation<?>> violationSet = ((ConstraintViolationException) ex).getConstraintViolations();
				if (CollectionUtils.isNotEmpty(violationSet)) {
					String message = violationSet.stream().map(v -> {
						return getText(v.getMessage());
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

	/**
	 * 取得国际化值
	 * 默认取简体中文值
	 *
	 * @param key 键
	 * @return 值
	 */
	default String getText(String key) {
		return getText(key, null, SystemContextHolder.getLocale());
	}

	/**
	 * 取得国际化值
	 *
	 * @param key    键
	 * @param locale 区域
	 * @return 值
	 */
	default String getText(String key, String locale) {
		return getText(key, null, locale);
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key  键
	 * @param args 参数
	 * @return 值
	 */
	default String getText(String key, Object[] args) {
		return getText(key, args, SystemContextHolder.getLocale());
	}

	/**
	 * 取得国际化值，并进行格式化
	 *
	 * @param key    键
	 * @param args   参数
	 * @param locale 区域
	 * @return 值
	 */
	String getText(String key, Object[] args, String locale);

//	/**
//	 * 设置I18nkey值
//	 *
//	 * @param key   键
//	 * @param value 值
//	 */
//	void setI18nValue(String key, String value);
//
//	/**
//	 * 设置I18nKey的值
//	 *
//	 * @param key    键
//	 * @param value  值
//	 * @param locale 区域
//	 */
//	void setI18nValue(String key, String value, String locale);
//
//	/**
//	 * 删除一个i18nKey
//	 *
//	 * @param key
//	 */
//	void removeI18nValue(String key);
//
//	/**
//	 * 删除一个i18nKey
//	 *
//	 * @param key
//	 * @param locale
//	 */
//	void removeI18nValue(String key, String locale);
//
//	/**
//	 * 取得所有的key
//	 *
//	 * @param locale 语言
//	 * @return 所有的国际化Key
//	 */
//	SortedMap<String, String> getAllKeys(String locale);
}
