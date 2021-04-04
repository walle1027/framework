package org.loed.framework.common.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addSerializer(Date.class, new DateSerializer(Boolean.FALSE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
		timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
		timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
		mapper.registerModule(timeModule).registerModule(new ParameterNamesModule()).registerModules(ObjectMapper.findModules());
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

	public static byte[] toBytes(Object object) {
		try {
			return mapper.writeValueAsBytes(object);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
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
