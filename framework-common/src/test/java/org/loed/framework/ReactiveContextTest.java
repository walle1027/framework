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
				.flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.get(key))))
				.contextWrite(ctx -> ctx.put(key, "World"));

		StepVerifier.create(r)
				.expectNext("Hello World")
				.verifyComplete();
	}

	@Test
	public void test2() {
		String key = "message";
		Mono<String> r = Mono.just("Hello")
				.contextWrite(ctx -> ctx.put(key, "World"))
				.flatMap(s -> Mono.deferContextual(ctx -> {
					return Mono.just(s + " " + ctx.getOrDefault(key, "Stranger"));
				}));

		StepVerifier.create(r)
				.expectNext("Hello Stranger")
				.verifyComplete();
	}

	@Test
	public void test3() {
		String key = "message";

		Mono<String> r = Mono.just(key).contextWrite(ctx -> ctx.put(key, "Hello"))
				.flatMap(s -> Mono.deferContextual(ctx -> Mono.just(ctx.getOrDefault(key, "Default"))));

		StepVerifier.create(r)
				.expectNext("Default")
				.verifyComplete();
	}

	@Test
	public void test4() {
		String key = "message";

		Mono<String> r = Mono.just("").contextWrite(ctx -> ctx.put(key, "Stranger"))
				.flatMap(s -> Mono.deferContextual(ctx -> Mono.just(ctx.getOrDefault(key, "Default"))))
				.contextWrite(ctx -> ctx.put(key, "Hello"));

		StepVerifier.create(r)
				.expectNext("Hello")
				.verifyComplete();
	}
}
