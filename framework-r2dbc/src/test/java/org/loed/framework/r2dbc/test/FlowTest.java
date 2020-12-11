package org.loed.framework.r2dbc.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.r2dbc.test.dao.StringIdDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.StringId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/12/4 9:34 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class FlowTest {
	@Autowired
	private StringIdDao stringIdDao;

	private SystemContext systemContext;

	@Before
	public void setUp() throws Exception {
		systemContext = new SystemContext();
		systemContext.setAccountId("r2dbc_test_account_id");
		systemContext.setTenantId("r2dbc_test_tenant_id");
		systemContext.setUserId("r2dbc_test_user_id");
	}

	@Test
	public void testSingleFlow() {
		Mono<Integer> result = createOneFlow();
		StepVerifier.create(result).expectNext(1).verifyComplete();
	}

	@Test
	public void testMultiFlow() {
		Flux<Integer> multiFlow = Flux.empty();
		int count = 3000;
		for (int i = 0; i < count; i++) {
			multiFlow = multiFlow.mergeWith(createOneFlow());
		}
		StepVerifier.create(multiFlow.collectList()).expectNextMatches(multiResult -> {
			Assert.assertEquals(multiResult.size(), count);
			return true;
		}).verifyComplete();
	}


	@Transactional
	public Mono<Integer> createOneFlow() {
		StringId po = new StringId();
		po.setProp1("StringId");
		po.setProp2(Integer.MAX_VALUE);
		po.setProp3(1.00d);
		po.setProp4(1.00f);
		po.setProp5(Long.MAX_VALUE);
		po.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
		po.setProp7(BigDecimal.ONE);
		po.setProp8(LocalDate.now());
		po.setProp9(LocalDateTime.now());
		po.setProp10(Boolean.TRUE);
		po.setProp11((byte) 0);
		po.setProp12(EnumProp.enum1);
		return stringIdDao.insert(po).flatMap(entity -> {
			entity.setProp1("StringIdUpdate");
			entity.setProp2(Integer.MIN_VALUE);
			entity.setProp3(2.00d);
			entity.setProp4(2.00f);
			entity.setProp5(Long.MIN_VALUE);
			entity.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
			entity.setProp7(BigDecimal.TEN);
			entity.setProp8(LocalDate.now());
			entity.setProp9(LocalDateTime.now());
			entity.setProp10(Boolean.FALSE);
			entity.setProp11((byte) 1);
			entity.setProp12(EnumProp.enum2);
			return stringIdDao.update(entity);
		}).flatMap(entity -> {
			entity.setProp1("StringIdUpdateWith");
			entity.setProp12(EnumProp.enum2);
			return stringIdDao.updateWith(entity, StringId::getProp1, StringId::getProp12);
		}).flatMap(entity -> {
			entity.setProp1("StringIdUpdateWithOut");
			entity.setProp12(EnumProp.enum3);
			return stringIdDao.updateWithout(entity, StringId::getProp3, StringId::getProp4);
		}).flatMap(entity -> {
			entity.setProp1(null);
			entity.setProp2(null);
			entity.setProp3(null);
			return stringIdDao.updateNonNull(entity);
		}).flatMap(entity -> {
			return stringIdDao.get(entity.getId());
		}).flatMap(entity -> {
			return stringIdDao.delete(entity.getId());
		});
	}


	@After
	public void tearDown() throws Exception {
		stringIdDao.execute("truncate table t_string_id", null).block();
	}
}
