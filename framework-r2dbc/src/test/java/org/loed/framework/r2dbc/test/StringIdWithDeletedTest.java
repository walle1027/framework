package org.loed.framework.r2dbc.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.test.dao.StringIdWithDeletedDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.StringIdWithDeleted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/10/12 1:55 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class StringIdWithDeletedTest {
	@Autowired
	private StringIdWithDeletedDao stringIdWithDeletedDao;

	private SystemContext systemContext;

	@Before
	public void setUp() throws Exception {
		systemContext = new SystemContext();
		systemContext.setAccountId("r2dbc_test_account_id");
		systemContext.setTenantId("r2dbc_test_tenant_id");
		systemContext.setUserId("r2dbc_test_user_id");
	}

	@Test
	public void testInsert() {
		Mono<StringIdWithDeleted> insert = stringIdWithDeletedDao.insert(insert()).flatMap(po -> {
			return stringIdWithDeletedDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(insert.log()).expectNextMatches(po -> {
			Assert.assertEquals(po.getVersion(), BigInteger.valueOf(0));
			Assert.assertEquals(po.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals((byte) po.getIsDeleted(), (byte) 0);
			Assert.assertEquals(po.getProp1(), "StringIdWithDeleted");
			Assert.assertEquals((int) po.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals(po.getProp3(), 1.00d, 2);
			Assert.assertEquals(po.getProp4(), 1.00f, 2);
			Assert.assertEquals((long) po.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(po.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
			Assert.assertEquals(po.getProp7().intValue(), BigDecimal.ONE.intValue());
			Assert.assertEquals(po.getProp10(), Boolean.TRUE);
			Assert.assertEquals((byte) po.getProp11(), (byte) 0);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdate() {
		Mono<StringIdWithDeleted> update = stringIdWithDeletedDao.insert(insert()).map(po -> {
			po.setProp1("testUpdate");
			po.setProp2(Integer.MIN_VALUE);
			po.setProp3(2.00D);
			po.setProp4(2.00F);
			po.setProp5(Long.MIN_VALUE);
			po.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
			po.setProp7(BigDecimal.TEN);
			po.setProp8(LocalDate.now());
			po.setProp9(LocalDateTime.now());
			po.setProp10(Boolean.FALSE);
			po.setProp11((byte) 1);
			po.setProp12(EnumProp.enum2);
			return po;
		}).flatMap(po -> {
			return stringIdWithDeletedDao.update(po);
		}).flatMap(po -> {
			return stringIdWithDeletedDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(po -> {
			Assert.assertEquals(po.getVersion(), BigInteger.valueOf(1L));
			Assert.assertEquals(po.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(po.getUpdateBy(), systemContext.getUserId());
			Assert.assertEquals((byte) po.getIsDeleted(), (byte) 0);
			Assert.assertEquals(po.getProp1(), "testUpdate");
			Assert.assertEquals((int) po.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals((double) po.getProp3(), 2.00d, 2);
			Assert.assertEquals((float) po.getProp4(), 2.00f, 2);
			Assert.assertEquals((long) po.getProp5(), Long.MIN_VALUE);
			Assert.assertEquals(po.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
			Assert.assertEquals(po.getProp7().intValue(), BigDecimal.TEN.intValue());
			Assert.assertEquals(po.getProp10(), Boolean.FALSE);
			Assert.assertEquals((byte) po.getProp11(), (byte) 1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testDelete() {
		Mono<Tuple2<StringIdWithDeleted, Integer>> delete = stringIdWithDeletedDao.insert(insert()).flatMap(po -> {
			return Mono.zip(Mono.just(po), stringIdWithDeletedDao.delete(po.getId()));
		}).flatMap(tup -> {
			String id = tup.getT1().getId();
			return Mono.zip(stringIdWithDeletedDao.get(id).defaultIfEmpty(new StringIdWithDeleted()), Mono.just(tup.getT2()));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNextMatches(tup -> {
			Assert.assertNull(tup.getT1().getId());
			Assert.assertEquals((int) tup.getT2(), 1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testDelete2() {
		Criteria<StringIdWithDeleted> criteria = Criteria.from(StringIdWithDeleted.class);
		int count = 1000;
		List<String> propList = new ArrayList<>();
		for (int i = 0; i < count - 1; i++) {
			propList.add("StringIdWithDeleted" + (i + 1));
		}
		criteria = criteria.and(StringIdWithDeleted::getProp1).in(propList);
		Mono<List<StringIdWithDeleted>> deleteResult = stringIdWithDeletedDao.batchInsert(batchInsert()).then(stringIdWithDeletedDao.delete(criteria)).then(stringIdWithDeletedDao.find(Criteria.from(StringIdWithDeleted.class)).collectList())
				.subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(deleteResult.log()).expectNextMatches(poList -> {
			Assert.assertEquals(poList.size(), 1);
			for (StringIdWithDeleted po : poList) {
				Assert.assertEquals(po.getVersion(), BigInteger.valueOf(0L));
				Assert.assertEquals(po.getCreateBy(), systemContext.getUserId());
				Assert.assertNull(po.getUpdateBy());
				Assert.assertNull(po.getUpdateTime());
				Assert.assertEquals((byte) po.getIsDeleted(), (byte) 0);
				Assert.assertEquals(po.getProp1(), "StringIdWithDeleted1000");
				Assert.assertEquals((int) po.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals((double) po.getProp3(), 1.00d, 2);
				Assert.assertEquals((float) po.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) po.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(po.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(po.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(po.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) po.getProp11(), (byte) 0);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testFind() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<List<StringIdWithDeleted>> find = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			Criteria<StringIdWithDeleted> criteria = Criteria.from(StringIdWithDeleted.class)
					.and(StringIdWithDeleted::getId).in(poList.stream().map(StringIdWithDeleted::getId).collect(Collectors.toList()))
					.and(StringIdWithDeleted::getProp1).contains("StringIdWithDeleted")
					.and(StringIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(StringIdWithDeleted::getProp3).is(1.00d)
					.and(StringIdWithDeleted::getProp4).is(1.00f)
					.and(StringIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(StringIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(StringIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(StringIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(StringIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(StringIdWithDeleted::getProp11).is((byte) 0);
			return stringIdWithDeletedDao.find(criteria);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (StringIdWithDeleted p : poList) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringIdWithDeleted"));
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals(p.getProp3(), 1.00d, 2);
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testFindOne() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<StringIdWithDeleted> find = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringIdWithDeleted> criteria = Criteria.from(StringIdWithDeleted.class)
					.and(StringIdWithDeleted::getId).is(poList.get(0).getId())
					.and(StringIdWithDeleted::getProp1).contains("StringIdWithDeleted")
					.and(StringIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(StringIdWithDeleted::getProp3).is(1.00d)
					.and(StringIdWithDeleted::getProp4).is(1.00f)
					.and(StringIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(StringIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(StringIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(StringIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(StringIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(StringIdWithDeleted::getProp11).is((byte) 0);
			return stringIdWithDeletedDao.findOne(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(p -> {
			Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringIdWithDeleted"));
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals(p.getProp3(), 1.00d, 2);
			Assert.assertEquals(p.getProp4(), 1.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.TRUE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testCount() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<Long> count = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringIdWithDeleted> criteria = Criteria.from(StringIdWithDeleted.class)
					.and(StringIdWithDeleted::getProp1).contains("StringIdWithDeleted")
					.and(StringIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(StringIdWithDeleted::getProp3).is(1.00d)
					.and(StringIdWithDeleted::getProp4).is(1.00f)
					.and(StringIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(StringIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(StringIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(StringIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(StringIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(StringIdWithDeleted::getProp11).is((byte) 0);
			return stringIdWithDeletedDao.count(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(count.log()).expectNext((long) longIdList.size()).verifyComplete();
	}

	@Test
	public void testFindByProperty() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<List<StringIdWithDeleted>> find = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			return stringIdWithDeletedDao.findByProperty(StringIdWithDeleted::getProp1, "StringIdWithDeleted1");
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (StringIdWithDeleted p : poList) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertNotNull(p.getCreateTime());
				Assert.assertNull(p.getUpdateBy());
				Assert.assertNull(p.getUpdateTime());
				Assert.assertEquals(p.getProp1(), "StringIdWithDeleted1");
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals(p.getProp3(), 1.00d, 2);
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testIsRepeated() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<Tuple2<Boolean, Boolean>> repeat = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			String id = poList.stream().filter(po -> po.getProp1().equals("StringIdWithDeleted1")).findAny().get().getId();
			return Mono.zip(stringIdWithDeletedDao.isRepeated(id, StringIdWithDeleted::getProp1, "StringIdWithDeleted1"),
					stringIdWithDeletedDao.isRepeated(null, StringIdWithDeleted::getProp1, "StringIdWithDeleted1"));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(repeat.log()).expectNextMatches(tup -> {
			return !tup.getT1() && tup.getT2();
		}).verifyComplete();
	}

	@Test
	public void testFindPage() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<Pagination<StringIdWithDeleted>> page = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringIdWithDeleted> criteria = Criteria.from(StringIdWithDeleted.class)
					.and(StringIdWithDeleted::getId).in(poList.stream().map(StringIdWithDeleted::getId).collect(Collectors.toList()))
					.and(StringIdWithDeleted::getProp1).contains("StringIdWithDeleted")
					.and(StringIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(StringIdWithDeleted::getProp3).is(1.00d)
					.and(StringIdWithDeleted::getProp4).is(1.00f)
					.and(StringIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(StringIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(StringIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(StringIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(StringIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(StringIdWithDeleted::getProp11).is((byte) 0)
					.asc(StringIdWithDeleted::getId);
			return stringIdWithDeletedDao.findPage(criteria, PageRequest.of(1, 10));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(page.log()).expectNextMatches(pg -> {
			Assert.assertEquals(pg.getTotal(), (long) longIdList.size());
			int i = 10;
			for (StringIdWithDeleted p : pg.getRows()) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertNotNull(p.getCreateTime());
				Assert.assertNull(p.getUpdateTime());
				Assert.assertNull(p.getUpdateBy());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringIdWithDeleted"));
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
				Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testSelect() {
		List<StringIdWithDeleted> longIdList = batchInsert();
		Mono<List<StringIdWithDeleted>> select = stringIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			String sql = "select * from t_string_id_with_deleted where prop1 like :prop1";
			Map<String, R2dbcParam> paramMap = new HashMap<>();
			paramMap.put("prop1", new R2dbcParam(String.class, "%StringIdWithDeleted%"));
			return stringIdWithDeletedDao.select(sql, paramMap);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(select.log()).expectNextMatches(poList -> {
			for (StringIdWithDeleted p : poList) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringIdWithDeleted"));
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals(p.getProp3(), 1.00d, 2);
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			}
			return true;
		}).verifyComplete();
	}

	private StringIdWithDeleted insert() {
		StringIdWithDeleted po = new StringIdWithDeleted();
		po.setProp1("StringIdWithDeleted");
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
		return po;
	}

	private List<StringIdWithDeleted> batchInsert() {
		int count = 1000;
		List<StringIdWithDeleted> longIds = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			StringIdWithDeleted po = new StringIdWithDeleted();
			po.setProp1("StringIdWithDeleted" + (i + 1));
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
			longIds.add(po);
		}
		return longIds;
	}

	@After
	public void tearDown() throws Exception {
		stringIdWithDeletedDao.execute("truncate table t_string_id_with_deleted", null).block();
	}
}
