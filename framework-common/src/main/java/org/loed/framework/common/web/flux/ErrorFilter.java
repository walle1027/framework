package org.loed.framework.common.web.flux;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.loed.framework.common.Message;
import org.loed.framework.common.Result;
import org.loed.framework.common.util.SerializeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 6:26 下午
 */
@Slf4j
public class ErrorFilter implements WebFilter {
	private ObjectMapper objectMapper;
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestUrl = exchange.getRequest().getURI().toString();
		return chain.filter(exchange).onErrorResume(err -> {
			log.error("error request for url {} caused by: {}", requestUrl, err.getMessage());
			Result<Void> result = new Result<>();
			result.setMessage(err.getMessage());
			result.setCode(Message.MSG_ERROR);
			DataBuffer db = new DefaultDataBufferFactory().wrap(Objects.requireNonNull(SerializeUtils.toBytes(result)));
			exchange.getResponse().setStatusCode(HttpStatus.OK);
			exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return exchange.getResponse().writeWith(Mono.just(db));
		});
	}
}
