package org.loed.framework.r2dbc;

import org.apache.logging.log4j.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.r2dbc.dao.PersonDao;
import org.loed.framework.r2dbc.po.CommonPO;
import org.loed.framework.r2dbc.po.Person;
import org.loed.framework.r2dbc.po.Sex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * @author Thomason
 * @version 1.0
 * @since 2020/7/8 23:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class PersonDaoTest {
	@Autowired
	private PersonDao personDao;

	private Map<String, String> contextMap;

	@Before
	public void setUp() throws Exception {
		contextMap = new HashMap<>();
		contextMap.put(SystemContext.CONTEXT_ACCOUNT_ID, "testAccountId");
		contextMap.put(SystemContext.CONTEXT_TENANT_CODE, "testTenantCode");
	}

	@Test
	public void testInsert() {
		Person person = new Person();
		String id = UUID.randomUUID().toString().replace("-", "");
		person.setId(id);
		person.setName("test");
		person.setSex(Sex.Female);
		Mono<String> idMono = personDao.insert(person)
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, contextMap);
				}).map(CommonPO::getId);
		StepVerifier.create(idMono.log()).expectNext(id).verifyComplete();
	}

	@Test
	public void testBatchInsert() {
		List<Person> personList = new ArrayList<>();
		List<String> idList = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			Person person = new Person();
			String id = UUID.randomUUID().toString().replace("-", "");
			person.setId(id);
			person.setName("test");
			personList.add(person);
			idList.add(id);
		}

		Flux<String> map = personDao.batchInsert(personList)
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, contextMap);
				}).map(CommonPO::getId).doOnNext(System.out::println);
		StepVerifier.create(map.log()).expectNextSequence(idList).verifyComplete();
	}

	@Test
	public void testQuery2() {
		Mono<Long> map = personDao.count("test")
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, contextMap);
				});

		StepVerifier.create(map).expectNext(1L).verifyComplete();
	}

	@After
	public void tearDown() throws Exception {
		Mono<Void> delete = personDao.deleteByCriteria(Criteria.from(Person.class)).then();
		StepVerifier.create(delete.log()).expectNext().verifyComplete();
	}
}
