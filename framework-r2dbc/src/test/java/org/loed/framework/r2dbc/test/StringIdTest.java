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
import org.loed.framework.common.query.PageRequest;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.r2dbc.query.R2dbcParam;
import org.loed.framework.r2dbc.test.dao.StringIdDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.StringId;
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
 * @since 2020/10/12 10:53 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = R2dbcApplication.class)
public class StringIdTest {
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
	public void testInsert() {
		Mono<StringId> po = stringIdDao.insert(insert()).map(StringId::getId).flatMap(id -> {
			return stringIdDao.get(id);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(po.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getProp1(), "StringId");
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals(p.getProp3(), 1.00d, 2);
			Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<List<StringId>> insertResult = stringIdDao.batchInsert(batchInsert()).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(insertResult.log()).expectNextMatches(insertList -> {
			for (int i = 0; i < insertList.size(); i++) {
				StringId p = insertList.get(i);
				String id = p.getId();
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals(p.getProp3(), 1.00d, 2);
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<StringId> update = stringIdDao.insert(insert()).map(po -> {
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
			return stringIdDao.update(po);
		}).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
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
		Mono<StringId> update = stringIdDao.insert(insert()).map(po -> {
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
			return stringIdDao.updateWith(po, StringId::getProp1, StringId::getProp2);
		}).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals(p.getProp3(), 1.00d, 2);
			Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<StringId> update = stringIdDao.insert(insert()).map(po -> {
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
			return stringIdDao.updateWithout(po, StringId::getProp1, StringId::getProp2);
		}).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getProp1(), "StringId");
			Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
			Assert.assertEquals(p.getProp3(), 2.00d, 2);
			Assert.assertEquals(p.getProp4(), 2.00f, 2);
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
		Mono<StringId> update = stringIdDao.insert(insert()).map(po -> {
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
			return stringIdDao.updateNonBlank(po);
		}).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertEquals(p.getProp3(), 1.00d, 2);
			Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<StringId> update = stringIdDao.insert(insert()).map(po -> {
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
			return stringIdDao.updateNonBlankAnd(po, StringId::getProp3);
		}).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(update.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 1L);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
			Assert.assertEquals(p.getProp1(), "testUpdate");
			Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
			Assert.assertNull(p.getProp3());
			Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<List<StringId>> batchUpdate = stringIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (StringId po : poList) {
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
			return stringIdDao.batchUpdate(poList);
		}).collectList().flatMapMany(updateList -> {
			return stringIdDao.find(Criteria.from(StringId.class).and(StringId::getId).in(updateList.stream().map(StringId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "testUpdate"));
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertEquals(p.getProp3(), 2.00d, 2);
				Assert.assertEquals(p.getProp4(), 2.00f, 2);
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
		Mono<List<StringId>> batchUpdate = stringIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (StringId po : poList) {
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
			return stringIdDao.batchUpdateWith(poList, StringId::getProp1, StringId::getProp2);
		}).collectList().flatMapMany(updateList -> {
			return stringIdDao.find(Criteria.from(StringId.class).and(StringId::getId).in(updateList.stream().map(StringId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "testUpdate"));
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
		Mono<List<StringId>> batchUpdate = stringIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (StringId po : poList) {
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
			return stringIdDao.batchUpdateWithout(poList, StringId::getProp1, StringId::getProp2);
		}).collectList().flatMapMany(updateList -> {
			return stringIdDao.find(Criteria.from(StringId.class).and(StringId::getId).in(updateList.stream().map(StringId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals(p.getProp3(), 2.00d, 2);
				Assert.assertEquals(p.getProp4(), 2.00f, 2);
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
		Mono<List<StringId>> batchUpdate = stringIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (StringId po : poList) {
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
			return stringIdDao.batchUpdateNonBlank(poList);
		}).collectList().flatMapMany(updateList -> {
			return stringIdDao.find(Criteria.from(StringId.class).and(StringId::getId).in(updateList.stream().map(StringId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "testUpdate"));
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertEquals(p.getProp3(), 1.00d, 2);
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<List<StringId>> batchUpdate = stringIdDao.batchInsert(batchInsert()).collectList().flatMapMany(poList -> {
			for (StringId po : poList) {
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
			return stringIdDao.batchUpdateNonBlankAnd(poList, StringId::getProp3);
		}).collectList().flatMapMany(updateList -> {
			return stringIdDao.find(Criteria.from(StringId.class).and(StringId::getId).in(updateList.stream().map(StringId::getId).collect(Collectors.toList())));
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(batchUpdate.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 1L);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getUpdateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "testUpdate"));
				Assert.assertEquals((int) p.getProp2(), Integer.MIN_VALUE);
				Assert.assertNull(p.getProp3());
				Assert.assertEquals(p.getProp4(), 1.00f, 2);
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
		Mono<StringId> get = stringIdDao.insert(insert()).flatMap(po -> {
			return stringIdDao.get(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(get.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
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
		Mono<Boolean> existsById = stringIdDao.insert(insert()).flatMap(po -> {
			return stringIdDao.existsById(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(existsById.log()).expectNext(true).verifyComplete();
	}

	@Test
	public void testDelete() {
		Mono<Integer> delete = stringIdDao.insert(insert()).flatMap(po -> {
			return stringIdDao.delete(po.getId());
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNext(1).verifyComplete();
	}

	@Test
	public void testDeleteByCriteria() {
		List<StringId> longIdList = batchInsert();
		Mono<Integer> delete = stringIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringId> criteria = Criteria.from(StringId.class).and(StringId::getProp1).contains("StringId")
					.and(StringId::getProp2).is(Integer.MAX_VALUE)
					.and(StringId::getProp3).is(1.00d)
					.and(StringId::getProp4).is(1.00f)
					.and(StringId::getProp5).is(Long.MAX_VALUE)
					.and(StringId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringId::getProp7).is(BigDecimal.ONE)
					.and(StringId::getProp8).lessEqual(LocalDate.now())
					.and(StringId::getProp9).lessEqual(LocalDateTime.now())
					.and(StringId::getProp10).is(Boolean.TRUE)
					.and(StringId::getProp11).is((byte) 0);
			return stringIdDao.delete(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(delete.log()).expectNext(longIdList.size()).verifyComplete();
	}

	@Test
	public void testFind() {
		List<StringId> longIdList = batchInsert();
		Mono<List<StringId>> find = stringIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			Criteria<StringId> criteria = Criteria.from(StringId.class)
					.and(StringId::getId).in(poList.stream().map(StringId::getId).collect(Collectors.toList()))
					.and(StringId::getProp1).contains("StringId")
					.and(StringId::getProp2).is(Integer.MAX_VALUE)
					.and(StringId::getProp3).is(1.00d)
					.and(StringId::getProp4).is(1.00f)
					.and(StringId::getProp5).is(Long.MAX_VALUE)
					.and(StringId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringId::getProp7).is(BigDecimal.ONE)
					.and(StringId::getProp8).lessEqual(LocalDate.now())
					.and(StringId::getProp9).lessEqual(LocalDateTime.now())
					.and(StringId::getProp10).is(Boolean.TRUE)
					.and(StringId::getProp11).is((byte) 0);
			return stringIdDao.find(criteria);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
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
		List<StringId> longIdList = batchInsert();
		Mono<StringId> find = stringIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringId> criteria = Criteria.from(StringId.class)
					.and(StringId::getId).is(poList.get(0).getId())
					.and(StringId::getProp1).contains("StringId")
					.and(StringId::getProp2).is(Integer.MAX_VALUE)
					.and(StringId::getProp3).is(1.00d)
					.and(StringId::getProp4).is(1.00f)
					.and(StringId::getProp5).is(Long.MAX_VALUE)
					.and(StringId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringId::getProp7).is(BigDecimal.ONE)
					.and(StringId::getProp8).lessEqual(LocalDate.now())
					.and(StringId::getProp9).lessEqual(LocalDateTime.now())
					.and(StringId::getProp10).is(Boolean.TRUE)
					.and(StringId::getProp11).is((byte) 0);
			return stringIdDao.findOne(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(p -> {
			Assert.assertEquals((long) p.getVersion(), 0);
			Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
			Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
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
		List<StringId> longIdList = batchInsert();
		Mono<Long> count = stringIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringId> criteria = Criteria.from(StringId.class)
					.and(StringId::getProp1).contains("StringId")
					.and(StringId::getProp2).is(Integer.MAX_VALUE)
					.and(StringId::getProp3).is(1.00d)
					.and(StringId::getProp4).is(1.00f)
					.and(StringId::getProp5).is(Long.MAX_VALUE)
					.and(StringId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringId::getProp7).is(BigDecimal.ONE)
					.and(StringId::getProp8).lessEqual(LocalDate.now())
					.and(StringId::getProp9).lessEqual(LocalDateTime.now())
					.and(StringId::getProp10).is(Boolean.TRUE)
					.and(StringId::getProp11).is((byte) 0);
			return stringIdDao.count(criteria);
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(count.log()).expectNext((long) longIdList.size()).verifyComplete();
	}

	@Test
	public void testFindByProperty() {
		List<StringId> longIdList = batchInsert();
		Mono<List<StringId>> find = stringIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			return stringIdDao.findByProperty(StringId::getProp1, "StringId1");
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(find.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertEquals(p.getProp1(), "StringId1");
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
		List<StringId> longIdList = batchInsert();
		Mono<Tuple2<Boolean, Boolean>> repeat = stringIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			String id = poList.stream().filter(po -> po.getProp1().equals("StringId1")).findFirst().get().getId();
			return Mono.zip(stringIdDao.isRepeated(id, StringId::getProp1, "StringId1"),
					stringIdDao.isRepeated(null, StringId::getProp1, "StringId1"));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(repeat.log()).expectNextMatches(tup -> {
			return !tup.getT1() && tup.getT2();
		}).verifyComplete();
	}

	@Test
	public void testFindPage() {
		List<StringId> longIdList = batchInsert();
		Mono<Pagination<StringId>> page = stringIdDao.batchInsert(longIdList).collectList().flatMap(poList -> {
			Criteria<StringId> criteria = Criteria.from(StringId.class)
					.and(StringId::getId).in(poList.stream().map(StringId::getId).collect(Collectors.toList()))
					.and(StringId::getProp1).contains("StringId")
					.and(StringId::getProp2).is(Integer.MAX_VALUE)
					.and(StringId::getProp3).is(1.00d)
					.and(StringId::getProp4).is(1.00f)
					.and(StringId::getProp5).is(Long.MAX_VALUE)
					.and(StringId::getProp6).is(BigInteger.valueOf(Long.MAX_VALUE))
					.and(StringId::getProp7).is(BigDecimal.ONE)
					.and(StringId::getProp8).lessEqual(LocalDate.now())
					.and(StringId::getProp9).lessEqual(LocalDateTime.now())
					.and(StringId::getProp10).is(Boolean.TRUE)
					.and(StringId::getProp11).is((byte) 0)
					.asc(StringId::getId);
			return stringIdDao.findPage(criteria, PageRequest.of(1, 10));
		}).subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(page.log()).expectNextMatches(pg -> {
			Assert.assertEquals(pg.getTotal(), (long) longIdList.size());
			int i = 10;
			for (StringId p : pg.getRows()) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
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
	public void testSelect() {
		List<StringId> longIdList = batchInsert();
		Mono<List<StringId>> select = stringIdDao.batchInsert(longIdList).collectList().flatMapMany(poList -> {
			String sql = "select * from t_string_id where prop1 like :prop1";
			Map<String, R2dbcParam> paramMap = new HashMap<>();
			paramMap.put("prop1", new R2dbcParam(String.class, "%StringId%"));
			return stringIdDao.select(sql, paramMap);
		}).collectList().subscriberContext(ctx -> ctx.put(ReactiveSystemContext.REACTIVE_SYSTEM_CONTEXT, systemContext));
		StepVerifier.create(select.log()).expectNextMatches(poList -> {
			for (StringId p : poList) {
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals(p.getCreateBy(), systemContext.getUserId());
				Assert.assertTrue(StringUtils.startsWith(p.getProp1(), "StringId"));
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

	private StringId insert() {
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
		return po;
	}

	private List<StringId> batchInsert() {
		int count = 1000;
		List<StringId> longIds = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			StringId po = new StringId();
			po.setProp1("StringId" + (i + 1));
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
		stringIdDao.execute("truncate table t_string_id", null).block();
	}
}
