package org.loed.framework;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/5/1 11:18 AM
 */
public class ReactiveContextTest {
	@Test
	public void test1() {
		String key = "message";
		Mono<String> r = Mono.just("Hello")
				.flatMap(s -> Mono.subscriberContext()
						.map(ctx -> s + " " + ctx.get(key)))
				.subscriberContext(ctx -> ctx.put(key, "World"));

		StepVerifier.create(r)
				.expectNext("Hello World")
				.verifyComplete();
	}

	@Test
	public void test2() {
		String key = "message";
		Mono<String> r = Mono.just("Hello")
				.subscriberContext(ctx -> ctx.put(key, "World"))
				.flatMap(s -> Mono.subscriberContext()
						.map(ctx -> {
							return s + " " + ctx.getOrDefault(key, "Stranger");
						}));

		StepVerifier.create(r)
				.expectNext("Hello Stranger")
				.verifyComplete();
	}

	@Test
	public void test3() {
		String key = "message";

		Mono<String> r = Mono.subscriberContext()
				.map(ctx -> ctx.put(key, "Hello"))
				.flatMap(ctx -> Mono.subscriberContext())
				.map(ctx -> ctx.getOrDefault(key, "Default"));

		StepVerifier.create(r)
				.expectNext("Default")
				.verifyComplete();
	}

	@Test
	public void test4() {
		String key = "message";

		Mono<String> r = Mono.subscriberContext()
				.map(ctx -> ctx.put(key, "Hello"))
				.subscriberContext(ctx -> {
					return ctx;
				})
				.map(ctx -> ctx.getOrDefault(key, "Default"));

		StepVerifier.create(r)
				.expectNext("Hello")
				.verifyComplete();
	}
}
