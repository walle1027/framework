package org.loed.framework.common.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;

/**
 * @author Thomason
 * @version 1.0
 * @since 11-10-19 上午12:10
 */

@SuppressWarnings({"unchecked"})
public class SerializeUtils {
	private static Logger logger = LoggerFactory.getLogger(SerializeUtils.class);
	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		SerializationConfig serializationConfig = mapper.getSerializationConfig();
		serializationConfig = serializationConfig.with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
				.without(SerializationFeature.WRITE_NULL_MAP_VALUES);
		mapper.setConfig(serializationConfig);
		// 忽略空值输出
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
		deserializationConfig = deserializationConfig.with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setConfig(deserializationConfig);
	}

	/**
	 * 将 java 对象序列化为byte[]
	 *
	 * @param object java对象
	 * @return 字节数组
	 * @throws IOException
	 */
	public static byte[] serialize(Object object) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * 将字节数组反序列化为java对象
	 *
	 * @param bytes 字节数组
	 * @return java 对象
	 */
	public static <X> X deSerialize(byte[] bytes) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objectInputStream;
		try {
			objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (X) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toJson(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	public static <X> X fromJson(String jsonStr, Class<X> x) {
		try {
			return mapper.readValue(jsonStr, x);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static <X> X fromJson(String jsonStr, TypeReference<X> typeReference) {
		try {
			return mapper.readValue(jsonStr, typeReference);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static <X> X fromJson(String jsonStr, Type type) {
		try {
			JavaType javaType = TypeFactory.defaultInstance().constructType(type);
			return mapper.readValue(jsonStr, javaType);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
