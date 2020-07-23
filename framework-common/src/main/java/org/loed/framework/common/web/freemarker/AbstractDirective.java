package org.loed.framework.common.web.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * freemarker宏的抽象类
 *
 * @author Thomason
 * @version 1.0
 * @since 11-9-12 下午10:50
 */

public abstract class AbstractDirective implements TemplateDirectiveModel {

	/**
	 * 从上下文中取出特定的值
	 *
	 * @param environment 上下文环境
	 * @param key         键
	 * @return 值
	 */
	public static String getStringFromEnvironment(Environment environment, String key) {
		return getStringFromEnvironment(environment, key, "");
	}

	/**
	 * 从上下文中取出特定的值
	 *
	 * @param environment  上下文环境
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return 值
	 */
	public static String getStringFromEnvironment(Environment environment, String key, String defaultValue) {
		try {
			String retVal = defaultValue;
			TemplateModel valModel = environment.getDataModel().get(key);
			if (valModel instanceof SimpleScalar) {
				retVal = ((SimpleScalar) valModel).getAsString();
			}
			if (valModel instanceof StringModel) {
				retVal = ((StringModel) valModel).getAsString();
			}
			return retVal;
		} catch (TemplateModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从参数中取得指定key的值
	 *
	 * @param params 参数map
	 * @param key    key
	 * @return 值
	 */
	public String getStringFromModel(Map params, String key) {
		return getStringFromModel(params, key, "");
	}

	/**
	 * 从参数中取得指定key的值
	 *
	 * @param params       参数map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return 值
	 */
	public String getStringFromModel(Map params, String key, String defaultValue) {
		String value = defaultValue;
		TemplateModel valueStr = (TemplateModel) params.get(key);
		if (valueStr != null) {
			if (valueStr instanceof SimpleScalar) {
				value = ((SimpleScalar) valueStr).getAsString();
			}
			if (valueStr instanceof StringModel) {
				value = ((StringModel) valueStr).getAsString();
			}
		}
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}
		return value;
	}

	public boolean getBooleanFromModel(Map params, String key, boolean defaultValue) {
		boolean result = defaultValue;
		TemplateModel hasEmptyModel = (TemplateModel) params.get(key);
		if (hasEmptyModel != null) {
			if (hasEmptyModel instanceof TemplateBooleanModel) {
				try {
					result = ((TemplateBooleanModel) hasEmptyModel)
							.getAsBoolean();
				} catch (TemplateModelException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public String getValueFromEnv(Environment env, String variableName, String defaultValue) throws TemplateModelException {
		TemplateModel templateModel = env.getVariable(variableName);
		if (templateModel == null) {
			return defaultValue;
		}
		String value = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (templateModel instanceof SimpleScalar) {
			value = ((SimpleScalar) templateModel).getAsString();
		} else if (templateModel instanceof StringModel) {
			value = ((StringModel) templateModel).getAsString();
		} else if (templateModel instanceof SimpleDate) {
			Date date = ((SimpleDate) templateModel).getAsDate();
			if (date != null) {
				value = sdf.format(date);
			}
		} else if (templateModel instanceof TemplateDateModel) {
			Date date = ((TemplateDateModel) templateModel).getAsDate();
			if (date != null) {
				value = sdf.format(date);
			}
		} else if (templateModel instanceof SimpleNumber) {
			Number number = ((SimpleNumber) templateModel).getAsNumber();
			if (number != null) {
				value = number.toString();
			}
		} else if (templateModel instanceof TemplateNumberModel) {
			Number number = ((TemplateNumberModel) templateModel).getAsNumber();
			if (number != null) {
				value = number.toString();
			}
		} else if (templateModel instanceof TemplateBooleanModel) {
			boolean b = ((TemplateBooleanModel) templateModel).getAsBoolean();
			value = String.valueOf(b);
		}
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public boolean getBooleanFromModel(Map params, String key) {
		return getBooleanFromModel(params, key, false);
	}
}
