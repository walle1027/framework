package org.loed.framework.r2dbc.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.PageRequest;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.test.dao.LongIdWithDeletedDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.LongIdWithDeleted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * @since 2020/10/10 4:56 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class LongIdWithDeletedTest {
	@Autowired
	private LongIdWithDeletedDao longIdWithDeletedDao;

	private SystemContext systemContext;

	@Before
	public void setUp() throws Exception {
		systemContext = new SystemContext();
		systemContext.setAccountId("1");
		systemContext.setTenantId("1");
		systemContext.setUserId("1");
	}

	@Test
	public void testInsert() {
		Mono<LongIdWithDeleted> insert = longIdWithDeletedDao.insert(insert()).flatMap(po -> {
			return longIdWithDeletedDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(insert.log()).expectNextMatches(po -> {
			Assert.assertEquals(po.getId(), BigInteger.valueOf(1L));
			Assert.assertEquals(po.getVersion(), BigInteger.valueOf(0));
			Assert.assertEquals(po.getCreateBy(), BigInteger.valueOf(1L));
			Assert.assertEquals((byte) po.getIsDeleted(), (byte) 0);
			Assert.assertEquals(po.getProp1(), "LongIdWithDeleted");
			Assert.assertEquals((int) po.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals((double) po.getProp3(), 1.00d, 2);
			Assert.assertEquals((float) po.getProp4(), 1.00f, 2);
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
		Mono<LongIdWithDeleted> update = longIdWithDeletedDao.insert(insert()).map(po -> {
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
			return longIdWithDeletedDao.update(po);
		}).flatMap(po -> {
			return longIdWithDeletedDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(po -> {
			Assert.assertEquals(po.getId(), BigInteger.valueOf(1L));
			Assert.assertEquals(po.getVersion(), BigInteger.valueOf(1L));
			Assert.assertEquals(po.getCreateBy(), BigInteger.valueOf(1L));
			Assert.assertEquals(po.getUpdateBy(), BigInteger.valueOf(1L));
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
		Mono<Tuple2<LongIdWithDeleted, Integer>> delete = longIdWithDeletedDao.insert(insert()).flatMap(po -> {
			return Mono.zip(Mono.just(po), longIdWithDeletedDao.delete(po.getId()));
		}).flatMap(tup -> {
			BigInteger id = tup.getT1().getId();
			return Mono.zip(longIdWithDeletedDao.get(id).defaultIfEmpty(new LongIdWithDeleted()), Mono.just(tup.getT2()));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNextMatches(tup -> {
			Assert.assertNull(tup.getT1().getId());
			Assert.assertEquals((int) tup.getT2(), 1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testDelete2() {
		Criteria<LongIdWithDeleted> criteria = Criteria.from(LongIdWithDeleted.class);
		criteria = criteria.and(LongIdWithDeleted::getId).lessThan(BigInteger.valueOf(1000L));
		Mono<List<LongIdWithDeleted>> deleteResult = longIdWithDeletedDao.batchInsert(batchInsert()).then(longIdWithDeletedDao.delete(criteria)).then(longIdWithDeletedDao.find(Criteria.from(LongIdWithDeleted.class)).collectList())
				.subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(deleteResult.log()).expectNextMatches(poList -> {
			Assert.assertEquals(poList.size(), 1);
			for (LongIdWithDeleted po : poList) {
				Assert.assertEquals(po.getId(), BigInteger.valueOf(1000L));
				Assert.assertEquals(po.getVersion(), BigInteger.valueOf(0L));
				Assert.assertEquals(po.getCreateBy(), BigInteger.valueOf(1L));
				Assert.assertNull(po.getUpdateBy());
				Assert.assertNull(po.getUpdateTime());
				Assert.assertEquals((byte) po.getIsDeleted(), (byte) 0);
				Assert.assertEquals(po.getProp1(), "LongIdWithDeleted1000");
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
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<List<LongIdWithDeleted>> find = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			Criteria<LongIdWithDeleted> criteria = Criteria.from(LongIdWithDeleted.class)
					.and(LongIdWithDeleted::getId).in(poList.stream().map(LongIdWithDeleted::getId).collect(Collectors.toList()))
					.and(LongIdWithDeleted::getProp1).contains("LongIdWithDeleted")
					.and(LongIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(LongIdWithDeleted::getProp3).is(1.00d)
					.and(LongIdWithDeleted::getProp4).is(1.00f)
					.and(LongIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(LongIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(LongIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(LongIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(LongIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(LongIdWithDeleted::getProp11).is((byte) 0);
			return longIdWithDeletedDao.find(criteria);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (LongIdWithDeleted p : poList) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), BigInteger.ONE);
				Assert.assertEquals(p.getProp1(), "LongIdWithDeleted" + p.getId());
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
	public void testFindOne() {
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<LongIdWithDeleted> find = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongIdWithDeleted> criteria = Criteria.from(LongIdWithDeleted.class)
					.and(LongIdWithDeleted::getId).is(poList.get(0).getId())
					.and(LongIdWithDeleted::getProp1).contains("LongIdWithDeleted")
					.and(LongIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(LongIdWithDeleted::getProp3).is(1.00d)
					.and(LongIdWithDeleted::getProp4).is(1.00f)
					.and(LongIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(LongIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(LongIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(LongIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(LongIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(LongIdWithDeleted::getProp11).is((byte) 0);
			return longIdWithDeletedDao.findOne(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(p -> {
			Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
			Assert.assertEquals(p.getCreateBy(), BigInteger.ONE);
			Assert.assertEquals(p.getProp1(), "LongIdWithDeleted" + p.getId());
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
			Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
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
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<Long> count = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongIdWithDeleted> criteria = Criteria.from(LongIdWithDeleted.class)
					.and(LongIdWithDeleted::getProp1).contains("LongIdWithDeleted")
					.and(LongIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(LongIdWithDeleted::getProp3).is(1.00d)
					.and(LongIdWithDeleted::getProp4).is(1.00f)
					.and(LongIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(LongIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(LongIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(LongIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(LongIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(LongIdWithDeleted::getProp11).is((byte) 0);
			return longIdWithDeletedDao.count(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(count.log()).expectNext((long) longIdList.size()).verifyComplete();
	}

	@Test
	public void testFindByProperty() {
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<List<LongIdWithDeleted>> find = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			return longIdWithDeletedDao.findByProperty(LongIdWithDeleted::getProp1, "LongId1");
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (LongIdWithDeleted p : poList) {
				Assert.assertEquals(p.getId(), BigInteger.ONE);
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), BigInteger.ONE);
				Assert.assertEquals(p.getProp1(), "LongId1");
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
	public void testIsRepeated() {
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<Tuple2<Boolean, Boolean>> repeat = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			return Mono.zip(longIdWithDeletedDao.isRepeated(BigInteger.ONE, LongIdWithDeleted::getProp1, "LongIdWithDeleted1"),
					longIdWithDeletedDao.isRepeated(null, LongIdWithDeleted::getProp1, "LongIdWithDeleted1"));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(repeat.log()).expectNextMatches(tup -> {
			return !tup.getT1() && tup.getT2();
		}).verifyComplete();
	}

	@Test
	public void testFindPage() {
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<Pagination<LongIdWithDeleted>> page = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongIdWithDeleted> criteria = Criteria.from(LongIdWithDeleted.class)
					.and(LongIdWithDeleted::getId).in(poList.stream().map(LongIdWithDeleted::getId).collect(Collectors.toList()))
					.and(LongIdWithDeleted::getProp1).contains("LongIdWithDeleted")
					.and(LongIdWithDeleted::getProp2).is(Integer.MAX_VALUE)
					.and(LongIdWithDeleted::getProp3).is(1.00d)
					.and(LongIdWithDeleted::getProp4).is(1.00f)
					.and(LongIdWithDeleted::getProp5).is(Long.MAX_VALUE)
					.and(LongIdWithDeleted::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongIdWithDeleted::getProp7).is(BigDecimal.ONE)
					.and(LongIdWithDeleted::getProp8).lessEqual(LocalDate.now())
					.and(LongIdWithDeleted::getProp9).lessEqual(LocalDateTime.now())
					.and(LongIdWithDeleted::getProp10).is(Boolean.TRUE)
					.and(LongIdWithDeleted::getProp11).is((byte) 0)
					.asc(LongIdWithDeleted::getId);
			return longIdWithDeletedDao.findPage(PageRequest.of(1, 10), criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(page.log()).expectNextMatches(pg -> {
			Assert.assertEquals(pg.getTotal(), (long) longIdList.size());
			int i = 0;
			for (LongIdWithDeleted p : pg.getRows()) {
				Assert.assertEquals(p.getId().intValue(), ++i);
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), BigInteger.ONE);
				Assert.assertEquals(p.getProp1(), "LongIdWithDeleted" + p.getId());
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
		List<LongIdWithDeleted> longIdList = batchInsert();
		Mono<List<LongIdWithDeleted>> select = longIdWithDeletedDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			String sql = "select * from t_long_id_with_deleted where prop1 like :prop1";
			Map<String, R2dbcParam> paramMap = new HashMap<>();
			paramMap.put("prop1", new R2dbcParam(String.class, "%LongIdWithDeleted%"));
			return longIdWithDeletedDao.select(sql, paramMap);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(select.log()).expectNextMatches(poList -> {
			for (LongIdWithDeleted p : poList) {
				Assert.assertEquals(p.getVersion(), BigInteger.ZERO);
				Assert.assertEquals(p.getCreateBy(), BigInteger.ONE);
				Assert.assertEquals(p.getProp1(), "LongIdWithDeleted" + p.getId());
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

	private LongIdWithDeleted insert() {
		LongIdWithDeleted po = new LongIdWithDeleted();
		po.setProp1("LongIdWithDeleted");
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

	private List<LongIdWithDeleted> batchInsert() {
		int count = 1000;
		List<LongIdWithDeleted> longIds = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			LongIdWithDeleted po = new LongIdWithDeleted();
			po.setProp1("LongIdWithDeleted" + (i + 1));
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
		longIdWithDeletedDao.execute("truncate table t_long_id_with_deleted", null).block();
	}
}
