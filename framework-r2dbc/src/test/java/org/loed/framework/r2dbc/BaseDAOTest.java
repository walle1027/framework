//package org.loed.framework.r2dbc;
//
//import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
//import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
//import dev.miku.r2dbc.mysql.constant.ZeroDateOption;
//import io.r2dbc.pool.ConnectionPool;
//import io.r2dbc.pool.ConnectionPoolConfiguration;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.loed.framework.common.SystemConstant;
//import org.loed.framework.r2dbc.dao.StudentDAO;
//import org.loed.framework.r2dbc.listener.impl.DefaultPreInsertListener;
//import org.loed.framework.r2dbc.listener.spi.PreInsertListener;
//import org.loed.framework.r2dbc.po.Student;
//import org.springframework.core.Ordered;
//import org.springframework.http.codec.ServerCodecConfigurer;
//import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
//import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.adapter.DefaultServerWebExchange;
//import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
//import org.springframework.web.server.session.DefaultWebSessionManager;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author thomason
// * @version 1.0
// * @since 2020/4/16 1:57 PM
// */
//@Slf4j
//public class BaseDAOTest {
//	private ConnectionPool connectionPool;
//
//	private StudentDAO studentDAO;
//
//	@Before
//	public void startup() {
//		MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
//				.host("127.0.0.1")
//				.username("root")
//				.port(3306) // optional, default 3306
//				.password("123456") // optional, default null, null means has no password
//				.database("test") // optional, default null, null means not specifying the database
//				.connectTimeout(Duration.ofSeconds(3)) // optional, default null, null means no timeout
////				.sslMode(SslMode.VERIFY_IDENTITY) // optional, default SslMode.PREFERRED
////				.sslCa("/path/to/mysql/ca.pem") // required when sslMode is VERIFY_CA or VERIFY_IDENTITY, default null, null means has no server CA cert
////				.sslKeyAndCert("/path/to/mysql/client-cert.pem", "/path/to/mysql/client-key.pem", "key-pem-password-in-here") // optional, default has no client key and cert
////				.tlsVersion(TlsVersions.TLS1_1, TlsVersions.TLS1_2, TlsVersions.TLS1_3) // optional, default is auto-selected by the server
//				.zeroDateOption(ZeroDateOption.USE_NULL) // optional, default ZeroDateOption.USE_NULL
//				.useServerPrepareStatement() // Use server-preparing statements, default use client-preparing statements
//				.build();
//		MySqlConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);
//		ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory).build();
//		ConnectionPool pool = new ConnectionPool(poolConfiguration);
//		studentDAO = new StudentDAO();
//		studentDAO.setConnectionPoolProxy(exchange -> pool);
//
//		PreInsertListener insertListener = new DefaultPreInsertListener();
//		((DefaultPreInsertListener) insertListener).setOrder(Ordered.HIGHEST_PRECEDENCE);
//		List<PreInsertListener> preInsertListeners = new ArrayList<>();
//		preInsertListeners.add(insertListener);
//		studentDAO.setPreInsertListeners(preInsertListeners);
//	}
//
//	@Test
//	public void testInsert() {
//		Student student = new Student();
//		student.setNo("test_r2dbc");
//		student.setName("test_r2dbc");
//		ServerWebExchange exchange = createExchange();
//		studentDAO.insert(exchange, student).map(t -> {
//			System.out.println("============>" + t);
//			throw new RuntimeException("errr");
////			return t;
//		}).map((s)->{
//			return studentDAO.commit(exchange).thenReturn(s);
//		}).doOnError(e->{
//			log.error(e.getMessage(),e);
//			 studentDAO.rollback(exchange);
//		}).doFinally(f->{
//			Mono.from(studentDAO.close(exchange));
//		}).block();
//		System.out.println(getClass());
//	}
//
//	private DefaultServerWebExchange createExchange() {
//		MockServerHttpRequest request = MockServerHttpRequest.get("https://example.com").build();
//		DefaultServerWebExchange exchange = createExchange(request);
//		exchange.getAttributes().put(SystemConstant.CONTEXT_TENANT_CODE, "r2dbc_test");
//		exchange.getAttributes().put(SystemConstant.CONTEXT_USER_ID, "r2dbc_test");
//		return exchange;
//	}
//
//	private DefaultServerWebExchange createExchange(MockServerHttpRequest request) {
//		return new DefaultServerWebExchange(request, new MockServerHttpResponse(),
//				new DefaultWebSessionManager(), ServerCodecConfigurer.create(),
//				new AcceptHeaderLocaleContextResolver());
//	}
//
//	@After
//	public void teardown() {
//		ConnectionPool pool = studentDAO.getConnectionPoolProxy().getConnectionPool(null);
//		pool.dispose();
//	}
//}
