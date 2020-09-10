package org.loed.framework.r2dbc;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	private SystemContext systemContext;

	@Before
	public void setUp() throws Exception {
		systemContext = new SystemContext();
		systemContext.setAccountId("testAccountId");
		systemContext.setTenantId("testTenantCode");
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
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
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
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
				}).map(CommonPO::getId).doOnNext(System.out::println);
		StepVerifier.create(map.log()).expectNextSequence(idList).verifyComplete();
	}

	@Test
	public void testQuery2() {
		Mono<Long> map = personDao.count("test")
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
				});

		StepVerifier.create(map).expectNextCount(1L).verifyComplete();
	}

	@Test
	public void testCustomQuery(){
		Mono<Integer> age = personDao.maxAge().defaultIfEmpty(1)
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
				});

		StepVerifier.create(age).expectNext(1).verifyComplete();
	}

	@After
	public void tearDown() throws Exception {
		Mono<Void> delete = personDao.delete(Criteria.from(Person.class)).then();
		StepVerifier.create(delete.log()).expectNext().verifyComplete();
	}
}
