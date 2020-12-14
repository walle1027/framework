package org.loed.framework.rpc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.BusinessException;
import org.loed.framework.common.Message;
import org.loed.framework.common.SpringUtils;
import org.loed.framework.common.SystemConstant;
import org.loed.framework.common.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/5/21 上午11:51
 */
@SuppressWarnings("Duplicates")
public class ServiceProxyFactoryBean<T> implements FactoryBean<T>, MethodInterceptor, InitializingBean {
	private static Logger logger = LoggerFactory.getLogger(ServiceProxyFactoryBean.class);

	private String baseUri;

	private WebClient webClient;

	@Autowired(required = false)
	private WebClient.Builder builder;

	private Class<T> serviceInterface;

	private Map<Method, MethodProfile> serviceMetaInfoMap = new ConcurrentHashMap<>();

	public ServiceProxyFactoryBean(Class<T> interfaze) {
		this.serviceInterface = interfaze;
	}

	private T proxy;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		if (isDefaultMethod(method)) {
			return invokeDefaultMethod(proxy, method, invocation.getArguments());
		}
		long start = System.currentTimeMillis();
		Object[] arguments = invocation.getArguments();
		String methodName = method.getName();
		if ("toString".equals(methodName) && method.getParameterTypes().length == 0) {
			return this.toString();
		} else if ("hashCode".equals(methodName) && method.getParameterTypes().length == 0) {
			return this.hashCode();
		} else if ("equals".equals(methodName) && method.getParameterTypes().length == 1) {
			return false;
		}
		MethodProfile methodProfile = serviceMetaInfoMap.computeIfAbsent(method, this::populateServiceProfile);

		Type type = method.getGenericReturnType();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		Annotation[][] arrays = method.getParameterAnnotations();
		Object httpBody = null;
		Map<String, Object> uriParams = new HashMap<>();
		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> multipartParams = new LinkedMultiValueMap<>();
		MediaType contentType = null;
		int argIndex = 0;
		for (Annotation[] annotations : arrays) {
			for (Annotation annotation : annotations) {
				Object argument = arguments[argIndex];
				if (annotation instanceof RequestBody) {
					httpBody = argument;
					contentType = MediaType.APPLICATION_JSON;
				} else if (annotation instanceof PathVariable) {
					PathVariable param = (PathVariable) annotation;
					uriParams.put(param.value(), argument);
				} else if (annotation instanceof RequestParam) {
					RequestParam param = (RequestParam) annotation;
					String name = param.name();
					if (StringUtils.isBlank(name)) {
						name = param.value();
					}
					if (StringUtils.isBlank(name)) {
						name = method.getParameters()[argIndex].getName();
					}
					String defaultValue = param.defaultValue();
					boolean required = param.required();
					if (argument == null) {
						if (required) {
							requestParams.add(name, defaultValue);
						}
					} else {
						addValue(requestParams, name, argument);
					}
					if (methodProfile.getHttpMethod() == HttpMethod.POST && contentType == null) {
						contentType = MediaType.APPLICATION_FORM_URLENCODED;
					}
				} else if (annotation instanceof RequestHeader) {
					String headerName = ((RequestHeader) annotation).value();
					String defaultValue = ((RequestHeader) annotation).defaultValue();
					if (StringUtils.isNotBlank(headerName)) {
						if (argument != null) {
							httpHeaders.add(headerName, String.valueOf(argument));
						} else {
							httpHeaders.add(headerName, defaultValue);
						}
					}
				} else if (annotation instanceof ModelAttribute) {
					Map<String, Object> map = object2Map(argument);
					if (map != null) {
						for (Map.Entry<String, Object> entry : map.entrySet()) {
							addValue(requestParams, entry.getKey(), entry.getValue());
						}
					}
				} else if (annotation instanceof RequestPart) {
					String name = ((RequestPart) annotation).name();
					String value = ((RequestPart) annotation).value();
					contentType = MediaType.MULTIPART_FORM_DATA;
					String paraName = name;
					if (StringUtils.isBlank(name)) {
						paraName = value;
					}
					if (StringUtils.isBlank(paraName)) {
						paraName = "file";
					}
					if (argument instanceof File) {
						FileInputStream fileInputStream = new FileInputStream((File) argument);
						InputStreamResource inputStreamSource = new InputStreamResource(fileInputStream);
						multipartParams.add(paraName, inputStreamSource);
					} else if (argument instanceof InputStream) {
						InputStreamResource inputStreamSource = new InputStreamResource((InputStream) argument);
						multipartParams.add(paraName, inputStreamSource);
					}
				}
			}
			argIndex++;
		}

		try {
			Mono<ClientResponse> exchange = null;
			if (methodProfile.getHttpMethod() == HttpMethod.GET) {
				exchange = webClient.get().uri(uriBuilder -> {
					uriBuilder.scheme(methodProfile.getSchema()).host(methodProfile.getHost()).port(methodProfile.getPort())
							.path(methodProfile.getPath());
					if (!requestParams.isEmpty()) {
						uriBuilder.queryParams(requestParams);
					}
					if (uriParams.isEmpty()) {
						return uriBuilder.build();
					} else {
						return uriBuilder.build(uriParams);
					}
				}).accept(MediaType.APPLICATION_JSON).exchange();
			} else if (methodProfile.getHttpMethod() == HttpMethod.POST) {
				MediaType finalContentType = contentType;
				WebClient.RequestBodySpec requestBodySpec = webClient.post().uri(uriBuilder -> {
					uriBuilder.scheme(methodProfile.getSchema()).host(methodProfile.getHost()).port(methodProfile.getPort())
							.path(methodProfile.getPath());
					if (!requestParams.isEmpty() && finalContentType == MediaType.APPLICATION_JSON) {
						uriBuilder.queryParams(requestParams);
					}
					if (uriParams.isEmpty()) {
						return uriBuilder.build();
					} else {
						return uriBuilder.build(uriParams);
					}
				}).accept(MediaType.APPLICATION_JSON).contentType(contentType);

				if (contentType == MediaType.MULTIPART_FORM_DATA) {
					if (!requestParams.isEmpty()) {
//						for (Map.Entry<String, List<String>> entry : requestParams.entrySet()) {
//							multipartParams.addAll(entry.getKey(), entry.getValue());
//						}
//						requestBodySpec.body(BodyInserters.fromFormData(requestParams));
					}
					requestBodySpec.body(BodyInserters.fromMultipartData(multipartParams));
				} else if (contentType == MediaType.APPLICATION_FORM_URLENCODED) {
					requestBodySpec.body(BodyInserters.fromFormData(requestParams));
				} else if (contentType == MediaType.APPLICATION_JSON) {
					if (httpBody != null) {
						requestBodySpec.body(BodyInserters.fromObject(httpBody));
					}
				}
				exchange = requestBodySpec.exchange();
			} else if (methodProfile.getHttpMethod() == HttpMethod.PUT) {
				MediaType finalContentType = contentType;
				WebClient.RequestBodySpec requestBodySpec = webClient.put().uri(uriBuilder -> {
					uriBuilder.scheme(methodProfile.getSchema()).host(methodProfile.getHost()).port(methodProfile.getPort())
							.path(methodProfile.getPath());
					if (!requestParams.isEmpty() && finalContentType == MediaType.APPLICATION_JSON) {
						uriBuilder.queryParams(requestParams);
					}
					if (uriParams.isEmpty()) {
						return uriBuilder.build();
					} else {
						return uriBuilder.build(uriParams);
					}
				}).accept(MediaType.APPLICATION_JSON).contentType(contentType);

				if (contentType == MediaType.MULTIPART_FORM_DATA) {
					requestBodySpec.body(BodyInserters.fromMultipartData(requestParams));
				} else if (contentType == MediaType.APPLICATION_FORM_URLENCODED) {
					requestBodySpec.body(BodyInserters.fromFormData(requestParams));
				} else if (contentType == MediaType.APPLICATION_JSON) {
					if (httpBody != null) {
						requestBodySpec.body(BodyInserters.fromObject(httpBody));
					}
				}
				exchange = requestBodySpec.exchange();
			} else if (methodProfile.getHttpMethod() == HttpMethod.DELETE) {
				exchange = webClient.delete().uri(uriBuilder -> {
					uriBuilder.scheme(methodProfile.getSchema()).host(methodProfile.getHost()).port(methodProfile.getPort())
							.path(methodProfile.getPath());
					if (!requestParams.isEmpty()) {
						uriBuilder.queryParams(requestParams);
					}
					if (uriParams.isEmpty()) {
						return uriBuilder.build();
					} else {
						return uriBuilder.build(uriParams);
					}
				}).accept(MediaType.APPLICATION_JSON).exchange();
			}
			if (exchange == null) {
				throw new BusinessException(new Message("error exchange", SystemConstant.MSG_ERROR));
			}
			Class<?> returnType = method.getReturnType();
			if (Flux.class.isAssignableFrom(returnType)) {
				Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
				ParameterizedTypeReference<Object> typeReference = ParameterizedTypeReference.forType(typeArgument);
				return exchange.flux().flatMap(clientResponse -> clientResponse.bodyToFlux(typeReference)).doOnError(e -> {
					logger.error(e.getMessage(), e);
				}).doFinally(s -> {
					long end = System.currentTimeMillis();
					logger.info("require remote url:{} cost[{}]ms", methodProfile.getBaseUri() + "/" + methodProfile.getPath(), (end - start));
				});
			} else if (Mono.class.isAssignableFrom(returnType)) {
				Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
				ParameterizedTypeReference<Object> typeReference = ParameterizedTypeReference.forType(typeArgument);
				return exchange.flatMap(clientResponse -> clientResponse.bodyToMono(typeReference)).doOnError(e -> {
					logger.error(e.getMessage(), e);
				}).doFinally(s -> {
					long end = System.currentTimeMillis();
					logger.info("require remote url:{} cost[{}]ms", methodProfile.getBaseUri() + "/" + methodProfile.getPath(), (end - start));
				});
			} else if (CompletableFuture.class.isAssignableFrom(returnType)) {
				Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
				ParameterizedTypeReference<Object> typeReference = ParameterizedTypeReference.forType(typeArgument);
				return exchange.flatMap(clientResponse -> clientResponse.bodyToMono(typeReference)).doOnError(e -> {
					logger.error(e.getMessage(), e);
				}).doFinally(s -> {
					long end = System.currentTimeMillis();
					logger.info("require remote url:{} cost[{}]ms", methodProfile.getBaseUri() + "/" + methodProfile.getPath(), (end - start));
				}).toFuture();
			} else if ("void".equals(returnType.getName())) {
				return exchange.flatMap(clientResponse -> {
					return Mono.empty();
				}).doOnError(e -> {
					logger.error(e.getMessage(), e);
				}).doFinally(s -> {
					long end = System.currentTimeMillis();
					logger.info("require remote url:{} cost[{}]ms", s.toString(), (end - start));
				}).block();
			} else {
				ParameterizedTypeReference<Object> typeReference = ParameterizedTypeReference.forType(method.getGenericReturnType());
				return exchange.flatMap(clientResponse -> clientResponse.bodyToMono(typeReference)).doOnError(e -> {
					logger.error(e.getMessage(), e);
				}).doFinally(s -> {
					long end = System.currentTimeMillis();
					logger.info("require remote url:{} cost[{}]ms", methodProfile.getBaseUri() + "/" + methodProfile.getPath(), (end - start));
				}).block();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(new Message("fail:" + e.getMessage(), SystemConstant.MSG_ERROR));
		}
	}

	private MethodProfile populateServiceProfile(Method method) {
		MethodProfile methodProfile = new MethodProfile();
		methodProfile.setBaseUri(baseUri);
		methodProfile.setWebClient(webClient);
		GetMapping getMapping = method.getAnnotation(GetMapping.class);
		if (getMapping != null) {
			methodProfile.setHttpMethod(HttpMethod.GET);
			methodProfile.setPath(getMapping.value()[0]);
		}
		PostMapping postMapping = method.getAnnotation(PostMapping.class);
		if (postMapping != null) {
			methodProfile.setHttpMethod(HttpMethod.POST);
			methodProfile.setPath(postMapping.value()[0]);
		}
		PutMapping putMapping = method.getAnnotation(PutMapping.class);
		if (putMapping != null) {
			methodProfile.setHttpMethod(HttpMethod.PUT);
			methodProfile.setPath(putMapping.value()[0]);
		}
		DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
		if (deleteMapping != null) {
			methodProfile.setHttpMethod(HttpMethod.DELETE);
			methodProfile.setPath(deleteMapping.value()[0]);
		}
		RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
		if (requestMapping != null) {
			methodProfile.setPath(requestMapping.value()[0]);
			RequestMethod requestMethod = requestMapping.method()[0];
			switch (requestMethod) {
				case GET:
					methodProfile.setHttpMethod(HttpMethod.GET);
					break;
				case POST:
					methodProfile.setHttpMethod(HttpMethod.POST);
					break;
				case PUT:
					methodProfile.setHttpMethod(HttpMethod.PUT);
					break;
				case DELETE:
					methodProfile.setHttpMethod(HttpMethod.DELETE);
					break;
				case HEAD:
					methodProfile.setHttpMethod(HttpMethod.HEAD);
					break;
				default:
					methodProfile.setHttpMethod(HttpMethod.POST);
					break;
			}
		}
		ServiceProxy methodProxy = method.getAnnotation(ServiceProxy.class);
		if (methodProxy != null) {
			if (StringUtils.isNotBlank(methodProxy.baseUri())) {
				methodProfile.setBaseUri(methodProxy.baseUri());
			}
			if (StringUtils.isNotBlank(methodProxy.webClientBeanName())) {
				methodProfile.setWebClient(SpringUtils.getBean(methodProxy.webClientBeanName()));
			}
		}
		//如果restTemplate 为null 则需要从上下文中放一个restTemplate
		if (methodProfile.getWebClient() == null) {
			String[] beanNames = SpringUtils.applicationContext.getBeanNamesForType(WebClient.class);
			if (beanNames != null && beanNames.length > 0) {
				methodProfile.setWebClient(SpringUtils.getBean(beanNames[0]));
			}
		}
		//check service profile
		methodProfile.check();
		return methodProfile;
	}

	private void addValue(MultiValueMap<String, String> map, String key, Object value) {
		if (value.getClass().isArray()) {
			//处理简单类型
			String simpleName = value.getClass().getSimpleName();
			if ("int[]".equals(simpleName)) {
				int[] values = (int[]) value;
				for (int i = 0; i < values.length; i++) {
					int v = values[i];
					map.add(key, v + "");
				}
			} else if ("long[]".equals(simpleName)) {
				long[] values = (long[]) value;
				for (int i = 0; i < values.length; i++) {
					long v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("char[]".equals(simpleName)) {
				char[] values = (char[]) value;
				for (int i = 0; i < values.length; i++) {
					char v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("double[]".equals(simpleName)) {
				double[] values = (double[]) value;
				for (int i = 0; i < values.length; i++) {
					double v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("byte[]".equals(simpleName)) {
				byte[] values = (byte[]) value;
				for (int i = 0; i < values.length; i++) {
					byte v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("short[]".equals(simpleName)) {
				short[] values = (short[]) value;
				for (int i = 0; i < values.length; i++) {
					short v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("boolean[]".equals(simpleName)) {
				boolean[] values = (boolean[]) value;
				for (int i = 0; i < values.length; i++) {
					boolean v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else if ("float[]".equals(simpleName)) {
				float[] values = (float[]) value;
				for (int i = 0; i < values.length; i++) {
					float v = values[i];
					map.add(key, String.valueOf(v));
				}
			} else {
				Object[] values = (Object[]) value;
				for (int i = 0; i < values.length; i++) {
					Object v = values[i];
					map.add(key, String.valueOf(v));
				}
			}
		} else if (value instanceof Iterable) {
			Iterable values = (Iterable) value;
			int i = 0;
			for (Object v : values) {
				map.add(key, String.valueOf(v));
			}
		} else {
			map.add(key, String.valueOf(value));
		}
	}

	private Map<String, Object> object2Map(Object object) {
		if (object == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<>(30);
		List<Field> fields = ReflectionUtils.getDeclaredFields(object);
		if (CollectionUtils.isNotEmpty(fields)) {
			for (Field field : fields) {
				map.put(field.getName(), ReflectionUtils.getFieldValue(object, field));
			}
		}
		return map;
	}

	@Override
	public T getObject() throws Exception {
		return proxy;
	}

	@Override
	public Class<?> getObjectType() {
		return serviceInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void setWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.builder != null) {
			this.webClient = builder.build();
		} else {
			this.webClient = WebClient.builder().build();
		}
		this.proxy = (T) new ProxyFactory(serviceInterface, this).getProxy(ClassUtils.getDefaultClassLoader());
	}

	private boolean isDefaultMethod(Method method) {
		return ((method.getModifiers()
				& (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
				&& method.getDeclaringClass().isInterface();
	}

	private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
			throws Throwable {
		final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
				.getDeclaredConstructor(Class.class, int.class);
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}
		final Class<?> declaringClass = method.getDeclaringClass();
		return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
				.unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
	}

}
