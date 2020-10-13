package org.loed.framework.r2dbc.test;

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
import org.loed.framework.r2dbc.test.dao.LongIdDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.LongId;
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
 * @since 2020/9/30 11:02 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class LongIdTest {
	@Autowired
	private LongIdDao longIdDao;

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
		Mono<LongId> po = longIdDao.insert(insert()).map(LongId::getId).flatMap(id -> {
			return longIdDao.get(id);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(po.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "LongId");
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.TRUE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 0);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchInsert() {
		Mono<List<LongId>> insertResult = longIdDao.batchInsert(batchInsert()).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(insertResult.log()).expectNextMatches(insertList -> {
			for (int i = 0; i < insertList.size(); i++) {
				LongId p = insertList.get(i);
				Long id = p.getId();
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId" + id);
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
//				Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdate() {
		Mono<LongId> update = longIdDao.insert(insert()).map(po -> {
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
			return longIdDao.update(po);
		}).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals((long) p.getUpdateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals((double) p.getProp3(), 2.00d, 2);
			Assert.assertEquals((float) p.getProp4(), 2.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MIN_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.FALSE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 1);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdateWith() {
		Mono<LongId> update = longIdDao.insert(insert()).map(po -> {
			po.setProp1("testUpdate");
			po.setProp2(Integer.MIN_VALUE);
			po.setProp3(2.01D);
			po.setProp4(2.02F);
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
			return longIdDao.updateWith(po, LongId::getProp1, LongId::getProp2);
		}).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals((long) p.getUpdateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
			Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.TRUE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 0);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdateWithout() {
		Mono<LongId> update = longIdDao.insert(insert()).map(po -> {
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
			return longIdDao.updateWithout(po, LongId::getProp1, LongId::getProp2);
		}).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals((long) p.getUpdateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "LongId");
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals((double) p.getProp3(), 2.00d, 2);
			Assert.assertEquals((float) p.getProp4(), 2.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MIN_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.FALSE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 1);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdateNonBlank() {
		Mono<LongId> update = longIdDao.insert(insert()).map(po -> {
			po.setProp1("testUpdate");
			po.setProp2(Integer.MIN_VALUE);
			po.setProp3(null);
			po.setProp4(null);
			po.setProp5(null);
			po.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
			po.setProp7(BigDecimal.TEN);
			po.setProp8(LocalDate.now());
			po.setProp9(LocalDateTime.now());
			po.setProp10(Boolean.FALSE);
			po.setProp11((byte) 1);
			po.setProp12(EnumProp.enum2);
			return po;
		}).flatMap(po -> {
			return longIdDao.updateNonBlank(po);
		}).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals((long) p.getUpdateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
			Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.FALSE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 1);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testUpdateNonBlankAnd() {
		Mono<LongId> update = longIdDao.insert(insert()).map(po -> {
			po.setProp1("testUpdate");
			po.setProp2(Integer.MIN_VALUE);
			po.setProp3(null);
			po.setProp4(null);
			po.setProp5(null);
			po.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
			po.setProp7(BigDecimal.TEN);
			po.setProp8(LocalDate.now());
			po.setProp9(LocalDateTime.now());
			po.setProp10(Boolean.FALSE);
			po.setProp11((byte) 1);
			po.setProp12(EnumProp.enum2);
			return po;
		}).flatMap(po -> {
			return longIdDao.updateNonBlankAnd(po, LongId::getProp3);
		}).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals((long) p.getUpdateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertNull(p.getProp3());
			Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
			Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
			Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
			Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
			Assert.assertEquals(p.getProp10(), Boolean.FALSE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 1);
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchUpdate() {
		Mono<List<LongId>> batchUpdate = longIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (LongId po : poList) {
				po.setProp1("testUpdate" + po.getId());
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
			}
			return longIdDao.batchUpdate(poList);
		}).collectList().flatMapMany(updateList -> {
			return longIdDao.find(Criteria.from(LongId.class).and(LongId::getId).in(updateList.stream().map(LongId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals((long) p.getUpdateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "testUpdate" + p.getId());
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertEquals((double) p.getProp3(), 2.00d, 2);
				Assert.assertEquals((float) p.getProp4(), 2.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MIN_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.FALSE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 1);
			}
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchUpdateWith() {
		Mono<List<LongId>> batchUpdate = longIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (LongId po : poList) {
				po.setProp1("testUpdate" + po.getId());
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
			}
			return longIdDao.batchUpdateWith(poList, LongId::getProp1, LongId::getProp2);
		}).collectList().flatMapMany(updateList -> {
			return longIdDao.find(Criteria.from(LongId.class).and(LongId::getId).in(updateList.stream().map(LongId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals((long) p.getUpdateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "testUpdate" + p.getId());
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
				Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.ONE.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			}

//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchUpdateWithout() {
		Mono<List<LongId>> batchUpdate = longIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (LongId po : poList) {
				po.setProp1("testUpdate" + po.getId());
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
			}
			return longIdDao.batchUpdateWithout(poList, LongId::getProp1, LongId::getProp2);
		}).collectList().flatMapMany(updateList -> {
			return longIdDao.find(Criteria.from(LongId.class).and(LongId::getId).in(updateList.stream().map(LongId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals((long) p.getUpdateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId" + p.getId());
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals((double) p.getProp3(), 2.00d, 2);
				Assert.assertEquals((float) p.getProp4(), 2.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MIN_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.FALSE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 1);
			}

//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchUpdateNonBlank() {
		Mono<List<LongId>> batchUpdate = longIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (LongId po : poList) {
				po.setProp1("testUpdate" + po.getId());
				po.setProp2(Integer.MIN_VALUE);
				po.setProp3(null);
				po.setProp4(null);
				po.setProp5(null);
				po.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
				po.setProp7(BigDecimal.TEN);
				po.setProp8(LocalDate.now());
				po.setProp9(LocalDateTime.now());
				po.setProp10(Boolean.FALSE);
				po.setProp11((byte) 1);
				po.setProp12(EnumProp.enum2);
			}
			return longIdDao.batchUpdateNonBlank(poList);
		}).collectList().flatMapMany(updateList -> {
			return longIdDao.find(Criteria.from(LongId.class).and(LongId::getId).in(updateList.stream().map(LongId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals((long) p.getUpdateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "testUpdate" + p.getId());
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertEquals((double) p.getProp3(), 1.00d, 2);
				Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.FALSE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 1);
			}

//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchUpdateNonBlankAnd() {
		Mono<List<LongId>> batchUpdate = longIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (LongId po : poList) {
				po.setProp1("testUpdate" + po.getId());
				po.setProp2(Integer.MIN_VALUE);
				po.setProp3(null);
				po.setProp4(null);
				po.setProp5(null);
				po.setProp6(BigInteger.valueOf(Long.MIN_VALUE));
				po.setProp7(BigDecimal.TEN);
				po.setProp8(LocalDate.now());
				po.setProp9(LocalDateTime.now());
				po.setProp10(Boolean.FALSE);
				po.setProp11((byte) 1);
				po.setProp12(EnumProp.enum2);
			}
			return longIdDao.batchUpdateNonBlankAnd(poList, LongId::getProp3);
		}).collectList().flatMapMany(updateList -> {
			return longIdDao.find(Criteria.from(LongId.class).and(LongId::getId).in(updateList.stream().map(LongId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals((long) p.getUpdateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "testUpdate" + p.getId());
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertNull(p.getProp3());
				Assert.assertEquals((float) p.getProp4(), 1.00f, 2);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MIN_VALUE));
				Assert.assertEquals(p.getProp7().intValue(), BigDecimal.TEN.intValue());
				Assert.assertEquals(p.getProp10(), Boolean.FALSE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 1);
			}
//			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testGet() {
		Mono<LongId> get = longIdDao.insert(insert()).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(get.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getId(), 1L);
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "LongId");
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
	public void testExistsById() {
		Mono<Boolean> existsById = longIdDao.insert(insert()).flatMap(po -> {
			return longIdDao.existsById(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(existsById.log()).expectNext(true).verifyComplete();
	}

	@Test
	public void testDelete() {
		Mono<Integer> delete = longIdDao.insert(insert()).flatMap(po -> {
			return longIdDao.delete(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNext(1).verifyComplete();
	}

	@Test
	public void testDeleteByCriteria() {
		List<LongId> longIdList = batchInsert();
		Mono<Integer> delete = longIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongId> criteria = Criteria.from(LongId.class).and(LongId::getProp1).contains("LongId")
					.and(LongId::getProp2).is(Integer.MAX_VALUE)
					.and(LongId::getProp3).is(1.00d)
					.and(LongId::getProp4).is(1.00f)
					.and(LongId::getProp5).is(Long.MAX_VALUE)
					.and(LongId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongId::getProp7).is(BigDecimal.ONE)
					.and(LongId::getProp8).lessEqual(LocalDate.now())
					.and(LongId::getProp9).lessEqual(LocalDateTime.now())
					.and(LongId::getProp10).is(Boolean.TRUE)
					.and(LongId::getProp11).is((byte) 0);
			return longIdDao.delete(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNext(longIdList.size()).verifyComplete();
	}

	@Test
	public void testFind() {
		List<LongId> longIdList = batchInsert();
		Mono<List<LongId>> find = longIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			Criteria<LongId> criteria = Criteria.from(LongId.class)
					.and(LongId::getId).in(poList.stream().map(LongId::getId).collect(Collectors.toList()))
					.and(LongId::getProp1).contains("LongId")
					.and(LongId::getProp2).is(Integer.MAX_VALUE)
					.and(LongId::getProp3).is(1.00d)
					.and(LongId::getProp4).is(1.00f)
					.and(LongId::getProp5).is(Long.MAX_VALUE)
					.and(LongId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongId::getProp7).is(BigDecimal.ONE)
					.and(LongId::getProp8).lessEqual(LocalDate.now())
					.and(LongId::getProp9).lessEqual(LocalDateTime.now())
					.and(LongId::getProp10).is(Boolean.TRUE)
					.and(LongId::getProp11).is((byte) 0);
			return longIdDao.find(criteria);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId" + p.getId());
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
		List<LongId> longIdList = batchInsert();
		Mono<LongId> find = longIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongId> criteria = Criteria.from(LongId.class)
					.and(LongId::getId).is(poList.get(0).getId())
					.and(LongId::getProp1).contains("LongId")
					.and(LongId::getProp2).is(Integer.MAX_VALUE)
					.and(LongId::getProp3).is(1.00d)
					.and(LongId::getProp4).is(1.00f)
					.and(LongId::getProp5).is(Long.MAX_VALUE)
					.and(LongId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongId::getProp7).is(BigDecimal.ONE)
					.and(LongId::getProp8).lessEqual(LocalDate.now())
					.and(LongId::getProp9).lessEqual(LocalDateTime.now())
					.and(LongId::getProp10).is(Boolean.TRUE)
					.and(LongId::getProp11).is((byte) 0);
			return longIdDao.findOne(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals((long) p.getCreateBy(), 1L);
			Assert.assertEquals(p.getProp1(), "LongId" + p.getId());
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
		List<LongId> longIdList = batchInsert();
		Mono<Long> count = longIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongId> criteria = Criteria.from(LongId.class)
					.and(LongId::getProp1).contains("LongId")
					.and(LongId::getProp2).is(Integer.MAX_VALUE)
					.and(LongId::getProp3).is(1.00d)
					.and(LongId::getProp4).is(1.00f)
					.and(LongId::getProp5).is(Long.MAX_VALUE)
					.and(LongId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongId::getProp7).is(BigDecimal.ONE)
					.and(LongId::getProp8).lessEqual(LocalDate.now())
					.and(LongId::getProp9).lessEqual(LocalDateTime.now())
					.and(LongId::getProp10).is(Boolean.TRUE)
					.and(LongId::getProp11).is((byte) 0);
			return longIdDao.count(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(count.log()).expectNext((long) longIdList.size()).verifyComplete();
	}

	@Test
	public void testFindByProperty() {
		List<LongId> longIdList = batchInsert();
		Mono<List<LongId>> find = longIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			return longIdDao.findByProperty(LongId::getProp1, "LongId1");
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getId(), 1L);
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
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
		List<LongId> longIdList = batchInsert();
		Mono<Tuple2<Boolean, Boolean>> repeat = longIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			return Mono.zip(longIdDao.isRepeated(1L, LongId::getProp1, "LongId1"),
					longIdDao.isRepeated(null, LongId::getProp1, "LongId1"));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(repeat.log()).expectNextMatches(tup -> {
			return !tup.getT1() && tup.getT2();
		}).verifyComplete();
	}

	@Test
	public void testFindPage() {
		List<LongId> longIdList = batchInsert();
		Mono<Pagination<LongId>> page = longIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<LongId> criteria = Criteria.from(LongId.class)
					.and(LongId::getId).in(poList.stream().map(LongId::getId).collect(Collectors.toList()))
					.and(LongId::getProp1).contains("LongId")
					.and(LongId::getProp2).is(Integer.MAX_VALUE)
					.and(LongId::getProp3).is(1.00d)
					.and(LongId::getProp4).is(1.00f)
					.and(LongId::getProp5).is(Long.MAX_VALUE)
					.and(LongId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(LongId::getProp7).is(BigDecimal.ONE)
					.and(LongId::getProp8).lessEqual(LocalDate.now())
					.and(LongId::getProp9).lessEqual(LocalDateTime.now())
					.and(LongId::getProp10).is(Boolean.TRUE)
					.and(LongId::getProp11).is((byte) 0)
					.asc(LongId::getId);
			return longIdDao.findPage(criteria, PageRequest.of(1, 10));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(page.log()).expectNextMatches(pg -> {
			Assert.assertEquals(pg.getTotal(), (long) longIdList.size());
			int i = 10;
			for (LongId p : pg.getRows()) {
				Assert.assertEquals((long) p.getId(), ++i);
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId" + p.getId());
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
		List<LongId> longIdList = batchInsert();
		Mono<List<LongId>> select = longIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			String sql = "select * from t_long_id where prop1 like :prop1";
			Map<String, R2dbcParam> paramMap = new HashMap<>();
			paramMap.put("prop1", new R2dbcParam(String.class, "%LongId%"));
			return longIdDao.select(sql, paramMap);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(select.log()).expectNextMatches(poList ->{
			for (LongId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId" + p.getId());
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

	private LongId insert() {
		LongId po = new LongId();
		po.setProp1("LongId");
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

	private List<LongId> batchInsert() {
		int count = 1000;
		List<LongId> longIds = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			LongId po = new LongId();
			po.setProp1("LongId" + (i + 1));
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
		longIdDao.execute("truncate table t_long_id", null).block();
	}
}
