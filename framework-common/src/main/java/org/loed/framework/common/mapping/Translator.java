package org.loed.framework.common.mapping;//package org.loed.framework.common.mapping;
//
//import org.apache.commons.beanutils.BeanUtils;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.loed.framework.common.mapping.config.PropertyConfig;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.script.Invocable;
//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//import javax.script.ScriptException;
//import java.lang.reflect.InvocationTargetException;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author Thomason
// * @version 1.0
// * @since 2017/3/9.10:48
// */
//public class Translator {
//	private static Logger logger = LoggerFactory.getLogger(Translator.class);
//
//	public <I, O> O translateObject(I input, List<PropertyConfig> propertyConfigList, Class<O> clazz) {
//		if (input == null) {
//			return null;
//		}
//		String inputJson = JSON.toJSONString(input);
//		String json = translateObject(inputJson, propertyConfigList);
//		if (json == null) {
//			return null;
//		}
//		return JSON.parseObject(json, clazz);
//	}
//
//	public static String translateObject(String inputJsonStr, List<PropertyConfig> propertyConfigList) {
//		if (inputJsonStr == null) {
//			return null;
//		}
//		JSONObject jsonObject = JSON.parseObject(inputJsonStr);
//		JSONObject result = translate(jsonObject, propertyConfigList);
//		if (result == null) {
//			return null;
//		}
//		return result.toJSONString();
//	}
//
//	public static JSONObject translate(JSONObject inputObject, List<PropertyConfig> propertyConfigList) {
//		if (inputObject == null) {
//			return null;
//		}
//		if (CollectionUtils.isEmpty(propertyConfigList)) {
//			return inputObject;
//		}
//		JSONObject result = new JSONObject();
//		for (PropertyConfig propertyConfig : propertyConfigList) {
//			try {
//				copyProperty(inputObject, result, propertyConfig);
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		return result;
//	}
//
//	public static JSONArray translate(JSONArray input, List<PropertyConfig> propertyConfigList) {
//		if (input == null) {
//			return null;
//		}
//		if (CollectionUtils.isEmpty(propertyConfigList)) {
//			return input;
//		}
//		JSONArray result = new JSONArray();
//		int size = input.size();
//		for (int i = 0; i < size; i++) {
//			Object object = input.get(i);
//			if (object instanceof JSONObject) {
//				JSONObject dest = new JSONObject();
//				for (PropertyConfig propertyConfig : propertyConfigList) {
//					try {
//						copyProperty((JSONObject) object, dest, propertyConfig);
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//					}
//				}
//				result.add(i, dest);
//			} else if (object instanceof JSONArray) {
//				JSONArray array = translate((JSONArray) object, propertyConfigList);
//				result.add(i, array);
//			}
//		}
//		return result;
//	}
//
//	private static void copyProperty(JSONObject src, JSONObject dest, PropertyConfig propertyConfig) {
//		String srcPropertyName = propertyConfig.getSrcPropertyName();
//		String destPropertyName = propertyConfig.getDestPropertyName();
//		//是否是表达式
//		if (propertyConfig.isFormula() && StringUtils.isNotBlank(propertyConfig.getScript())) {
//			//处理
//			String value = executeWithJavaScript(propertyConfig.getScript(), src);
//			if (value != null) {
//				//目前仅仅做对象属性的赋值,其他的扩展暂不处理
//				setObject(dest, destPropertyName, value);
//			}
//			return;
//		}
//		//数组
//		if (srcPropertyName.contains("[]")) {
//			if (!destPropertyName.contains("[]")) {
//				//暂不支持从数组copy到非数组元素
//				logger.error("not support copy array{} to object{}", srcPropertyName, destPropertyName);
//				return;
//			}
//			if (sizeOf(srcPropertyName, "[]") != sizeOf(destPropertyName, "[]")) {
//				//暂不支持数组层级不一致的copy
//				logger.error("not support copy different depth array{}{}", srcPropertyName.lastIndexOf("[]") + 1, destPropertyName.lastIndexOf("[]") + 1);
//				return;
//			}
//			String substring = srcPropertyName.substring(0, srcPropertyName.indexOf("[]"));
//			JSONArray jsonArray = getJSONArray(src, substring);
//
//			if (jsonArray != null) {
//				String destSubString = destPropertyName.substring(0, destPropertyName.indexOf("[]"));
//				JSONArray destArray = createDestArray(dest, destSubString);
//				int size = jsonArray.size();
//				PropertyConfig subConfig = new PropertyConfig();
//				String subSrc = srcPropertyName.substring(srcPropertyName.indexOf("[]") + 2);
//				subConfig.setSrcPropertyName(subSrc.startsWith(".") ? subSrc.substring(1) : subSrc);
//				String subDest = destPropertyName.substring(destPropertyName.indexOf("[]") + 2);
//				subConfig.setDestPropertyName(subDest.startsWith(".") ? subDest.substring(1) : subDest);
//				subConfig.setSrcPropertyType(propertyConfig.getSrcPropertyType());
//				subConfig.setDestPropertyType(propertyConfig.getDestPropertyType());
//				for (int i = 0; i < size; i++) {
//					JSONObject arrayObject = (JSONObject) jsonArray.get(i);
//					JSONObject destObject = null;
//					if (i < destArray.size()) {
//						destObject = (JSONObject) destArray.get(i);
//					} else {
//						destObject = new JSONObject();
//						destArray.add(i, destObject);
//					}
//					copyProperty(arrayObject, destObject, subConfig);
//				}
//			}
//		}
//		//复杂对象
//		else {
//			Object object = getObject(src, srcPropertyName);
//			//TODO　数据类型转换
//			if (object != null) {
//				setObject(dest, destPropertyName, object);
//			}
//		}
//	}
//
//	private static int sizeOf(String input, String pattern) {
//		int size = 0;
//		String tmp = input;
//		while (tmp.contains(pattern)) {
//			size++;
//			tmp = tmp.substring(tmp.indexOf(pattern) + pattern.length());
//		}
//		return size;
//	}
//
//	private static JSONArray getJSONArray(JSONObject object, String propertyPath) {
//		if (propertyPath.contains(".")) {
//			String substring = propertyPath.substring(0, propertyPath.indexOf("."));
//			JSONObject jsonObject = object.getJSONObject(substring);
//			if (jsonObject != null) {
//				return getJSONArray(jsonObject, propertyPath.substring(propertyPath.indexOf(".") + 1));
//			}
//		} else {
//			if (propertyPath.endsWith("[]")) {
//				String substring = propertyPath.substring(0, propertyPath.length() - 2);
//				return object.getJSONArray(substring);
//			} else {
//				return object.getJSONArray(propertyPath);
//			}
//		}
//		return null;
//	}
//
//	private static Object getObject(JSONObject object, String propertyPath) {
//		if (propertyPath.contains(".")) {
//			String substring = propertyPath.substring(0, propertyPath.indexOf("."));
//			JSONObject jsonObject = object.getJSONObject(substring);
//			if (jsonObject != null) {
//				return getObject(jsonObject, propertyPath.substring(propertyPath.indexOf(".") + 1));
//			}
//		} else if (StringUtils.isEmpty(propertyPath)) {
//			return object;
//		} else {
//			return object.get(propertyPath);
//		}
//		return null;
//	}
//
//	private static JSONArray createDestArray(JSONObject dest, String propertyPath) {
//		if (propertyPath.contains(".")) {
//			String substring = propertyPath.substring(0, propertyPath.indexOf("."));
//			dest.putIfAbsent(substring, new JSONObject());
//			JSONObject jsonObject = (JSONObject) dest.get(substring);
//			return getJSONArray(jsonObject, propertyPath.substring(propertyPath.indexOf(".") + 1));
//		} else {
//			if (propertyPath.endsWith("[]")) {
//				String substring = propertyPath.substring(0, propertyPath.length() - 2);
//				dest.putIfAbsent(substring, new JSONArray());
//				return (JSONArray) dest.get(substring);
//			} else {
//				dest.putIfAbsent(propertyPath, new JSONArray());
//				return (JSONArray) dest.get(propertyPath);
//			}
//		}
//	}
//
//	private static void setObject(JSONObject dest, String propertyPath, Object value) {
//		if (propertyPath.contains(".")) {
//			String substring = propertyPath.substring(0, propertyPath.indexOf("."));
//			dest.putIfAbsent(substring, new JSONObject());
//			JSONObject jsonObject = (JSONObject) dest.get(substring);
//			setObject(jsonObject, propertyPath.substring(propertyPath.indexOf(".") + 1), value);
//		} else if (StringUtils.isEmpty(propertyPath)) {
//			if (value instanceof JSONObject) {
//				for (Map.Entry<String, Object> entry : ((JSONObject) value).entrySet()) {
//					dest.put(entry.getKey(), entry.getValue());
//				}
//			} else {
//				try {
//					dest.putAll(BeanUtils.describe(value));
//				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//					e.printStackTrace();
//				}
//			}
//		} else {
//			dest.put(propertyPath, value);
//		}
//	}
//
//	private static String executeWithJavaScript(String script, JSONObject inputJson) {
//		try {
//			logger.debug("start calculate with script:[" + script + "]");
//			logger.debug("parameter is:" + inputJson);
//			//创建计算引擎
//			ScriptEngineManager manager = new ScriptEngineManager();
//			ScriptEngine engine = manager.getEngineByName("javascript");
//			//js代码构建器
//			//解析动态脚本语言
//			engine.eval(script);
//			Invocable invokeEngine = (Invocable) engine;
//			//传入参数进行计算
//			String retValue = invokeEngine.invokeFunction("execute", inputJson).toString();
//			logger.debug("calculate formula:[" + script + "] with parameter:" + inputJson);
//			logger.debug("result is:[" + retValue + "]");
//			return retValue;
//		} catch (ScriptException | NoSuchMethodException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//}
