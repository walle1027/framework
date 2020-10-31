package org.loed.framework.common.web.flux.rewrite;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.SystemConstant;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/10 10:22 AM
 */
@Deprecated
public class ResponseBodyWrapperFilter implements WebFilter {
	private final List<PathPattern> ignorePathPatternList;

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	private HandlerMapping handlerMapping;
	private final ReactiveAdapterRegistry adapterRegistry;

	public ResponseBodyWrapperFilter(ReactiveAdapterRegistry adapterRegistry) {
		this.adapterRegistry = adapterRegistry;
		this.ignorePathPatternList = new ArrayList<>();
		//swagger-ui 的忽略列表
		ignorePathPatternList.add(new PathPatternParser().parse("/swagger-ui.html"));
		ignorePathPatternList.add(new PathPatternParser().parse("/webjars/**"));
		ignorePathPatternList.add(new PathPatternParser().parse("/swagger-resources/**"));
		ignorePathPatternList.add(new PathPatternParser().parse("/v2/api-docs"));
		ignorePathPatternList.add(new PathPatternParser().parse("/health"));
	}

	public void ignorePath(String ignore) {
		this.ignorePathPatternList.add(new PathPatternParser().parse(ignore));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		RequestPath path = exchange.getRequest().getPath();
		for (PathPattern pathPattern : ignorePathPatternList) {
			if (pathPattern.matches(path.pathWithinApplication())) {
				return chain.filter(exchange);
			}
		}

		HandlerMethod handlerMethod = (HandlerMethod) handlerMapping.getHandler(exchange).toProcessor().peek();
		MethodParameter returnType = handlerMethod.getReturnType();
		ResolvableType bodyType = ResolvableType.forMethodParameter(returnType);
		ReactiveAdapter adapter = adapterRegistry.getAdapter(bodyType.resolve());

		ResolvableType elementType;
		if (adapter != null) {
			ResolvableType genericType = bodyType.getGeneric();
			elementType = getElementType(adapter, genericType);
		} else {
			elementType = bodyType;
		}
		exchange.getRequest().getPath();
		return chain.filter(exchange.mutate().response(decorate(exchange, elementType)).build());
	}

	/**
	 * 这段代码是从springcloud-gateway中复制的
	 *
	 * @param exchange   交换机
	 * @param returnType 返回类型
	 * @return 响应结果
	 */
	ServerHttpResponse decorate(ServerWebExchange exchange, ResolvableType returnType) {
		return new ServerHttpResponseDecorator(exchange.getResponse()) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				HttpHeaders httpHeaders = new HttpHeaders();
				// explicitly add it in this way instead of
				// 'httpHeaders.setContentType(originalResponseContentType)'
				// this will prevent exception in case of using non-standard media
				// types like "Content-Type: image"
				httpHeaders.add(HttpHeaders.CONTENT_TYPE,
						MediaType.APPLICATION_JSON_VALUE);

				ClientResponse clientResponse = ClientResponse
						.create(exchange.getResponse().getStatusCode())
						.headers(headers -> headers.putAll(httpHeaders))
						.body(Flux.from(body)).build();

				// TODO: flux or mono
				Mono modifiedBody = clientResponse.bodyToMono(String.class)
						.flatMap(originalBody -> {
							//TODO 如果原始对象已经是被包装过的对象
							Object responseWrapped = exchange.getAttribute(SystemConstant.RESPONSE_WRAPPED);
							if (responseWrapped != null) {
								return Mono.just(originalBody);
							} else {
								//auto wrap response
								StringBuilder builder = new StringBuilder();
								builder.append("{\"code\":0");
								builder.append(",\"data\":");
								if (returnType.isAssignableFrom(String.class) || returnType.isAssignableFrom(Character.class)) {
									if (StringUtils.equalsIgnoreCase("null", originalBody) || StringUtils.isBlank(originalBody)) {
										builder.append("null");
									} else {
										builder.append("\"").append(originalBody).append("\"");
									}
								} else {
									if (StringUtils.equalsIgnoreCase("null", originalBody) || StringUtils.isBlank(originalBody)) {
										builder.append("null");
									} else {
										builder.append(originalBody);
									}
								}
								builder.append("}");
								return Mono.just(builder.toString());
							}
						});

				BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody,
						String.class);
				CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
						exchange, exchange.getResponse().getHeaders());
				return bodyInserter.insert(outputMessage, new BodyInserterContext())
						.then(Mono.defer(() -> {
							Flux<DataBuffer> messageBody = outputMessage.getBody();
							HttpHeaders headers = getDelegate().getHeaders();
							if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
								messageBody = messageBody.doOnNext(data -> headers
										.setContentLength(data.readableByteCount()));
							}
							// TODO: fail if isStreamingMediaType?
							return getDelegate().writeWith(messageBody);
						}));
			}

			@Override
			public Mono<Void> writeAndFlushWith(
					Publisher<? extends Publisher<? extends DataBuffer>> body) {
				return writeWith(Flux.from(body).flatMapSequential(p -> p));
			}
		};
	}

	private ResolvableType getElementType(ReactiveAdapter adapter, ResolvableType genericType) {
		if (adapter.isNoValue()) {
			return ResolvableType.forClass(Void.class);
		} else if (genericType != ResolvableType.NONE) {
			return genericType;
		} else {
			return ResolvableType.forClass(Object.class);
		}
	}
}
