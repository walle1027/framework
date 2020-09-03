package org.loed.framework.r2dbc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.r2dbc.dao.PeopleDao;
import org.loed.framework.r2dbc.po.People;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/11 9:12 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class PeopleDaoTest {
	@Autowired
	private PeopleDao peopleDao;

	private SystemContext systemContext;

	@Before
	public void setUp() {
		systemContext = new SystemContext();
		systemContext.setAccountId("testAccountId");
		systemContext.setTenantCode("testTenantCode");
	}

	@Test
	public void testInsert() {
		People people = new People();
		people.setName("张三");
		people.setSex((byte) 0);
		people.setRace("han");
		people.setHeight(new BigDecimal("182.1"));
		people.setWeight(68.2);
		Mono<String> idMono = peopleDao.insert(people)
				.subscriberContext(context -> {
					return context.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext);
				}).map(p -> {
					System.out.println("auto increasement id is:" + p.getId());
					return p.getName();
				});
		StepVerifier.create(idMono.log()).expectNext("张三").verifyComplete();
	}


	@Test
	public void testBatchInsert() {
		int count = 300;
		List<People> peopleList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			People people = new People();
			people.setName("test" + i);
			people.setSex((byte) 0);
			people.setRace("han");
			people.setHeight(new BigDecimal(i * 100.23));
			people.setWeight(i * 56.2);
			peopleList.add(people);
		}
		Mono<Integer> batch = peopleDao.batchInsert(peopleList)
				.doOnNext(people -> {
					System.out.println("auto gen id is:" + people.getId() + " and name is :" + people.getName());
				})
				.collectList().map(List::size);
		StepVerifier.create(batch.log()).expectNext(count).verifyComplete();
	}
}
