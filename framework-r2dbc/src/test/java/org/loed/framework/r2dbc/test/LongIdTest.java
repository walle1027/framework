package org.loed.framework.r2dbc.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.ReactiveSystemContext;
import org.loed.framework.common.context.SystemContext;
import org.loed.framework.r2dbc.test.dao.LongIdDao;
import org.loed.framework.r2dbc.test.po.EnumProp;
import org.loed.framework.r2dbc.test.po.LongId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
			Assert.assertEquals(p.getProp7(), BigDecimal.ONE);
			Assert.assertEquals(p.getProp10(), Boolean.TRUE);
			Assert.assertEquals((byte) p.getProp11(), (byte) 0);
			Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			return true;
		}).verifyComplete();
	}

	@Test
	public void testBatchInsert() {
		Mono<List<LongId>> insertResult = longIdDao.batchInsert(batchInsert()).flatMap(po -> {
			return longIdDao.get(po.getId());
		}).collectList();
		StepVerifier.create(insertResult.log()).expectNextMatches(insertList -> {
			for (int i = 0; i < insertList.size(); i++) {
				LongId p = insertList.get(i);
				Assert.assertEquals((long) p.getId(), i + 1L);
				Assert.assertEquals((long) p.getVersion(), 0);
				Assert.assertEquals((long) p.getCreateBy(), 1L);
				Assert.assertEquals(p.getProp1(), "LongId");
				Assert.assertEquals((int) p.getProp2(), Integer.MAX_VALUE);
				Assert.assertEquals((long) p.getProp5(), Long.MAX_VALUE);
				Assert.assertEquals(p.getProp6(), BigInteger.valueOf(Long.MAX_VALUE));
				Assert.assertEquals(p.getProp7(), BigDecimal.ONE);
				Assert.assertEquals(p.getProp10(), Boolean.TRUE);
				Assert.assertEquals((byte) p.getProp11(), (byte) 0);
				Assert.assertEquals(p.getProp12(), EnumProp.enum1);
			}
			return true;
		}).verifyComplete();
	}

	private LongId insert() {
		LongId po = new LongId();
		po.setProp1("LongId");
		po.setProp2(Integer.MAX_VALUE);
		po.setProp3(Double.MAX_VALUE);
		po.setProp4(1.23f);
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
			po.setProp1("LongId");
			po.setProp2(Integer.MAX_VALUE);
			po.setProp3(Double.MAX_VALUE);
			po.setProp4(1.23f);
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
