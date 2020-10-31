package org.loed.framework.common.context;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/10/31 19:42
 */
public class SystemContextExchangeFilter implements ExchangeFilterFunction {
	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return ReactiveSystemContext.getSystemContext().flatMap(context -> {
			ClientRequest.Builder requestBuilder = ClientRequest.from(request);
			List<Pair<String, String>> pairs = context.toHeaders();
			if (!pairs.isEmpty()) {
				for (Pair<String, String> pair : pairs) {
					requestBuilder.header(pair.getKey(), pair.getValue());
				}
			}
			return next.exchange(requestBuilder.build());
		});
	}
}
