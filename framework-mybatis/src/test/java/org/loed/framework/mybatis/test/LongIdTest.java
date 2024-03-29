package org.loed.framework.mybatis.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.mybatis.test.mapper.LongIdIsDeletedPOMapper;
import org.loed.framework.mybatis.test.mapper.LongIdPOMapper;
import org.loed.framework.mybatis.test.po.LongIdIsDeletedPO;
import org.loed.framework.mybatis.test.po.LongIdPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/25 3:31 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MybatisTestApplication.class)
public class LongIdTest {
	@Autowired
	private LongIdPOMapper longIdPOMapper;
	@Autowired
	private LongIdIsDeletedPOMapper longIdIsDeletedPOMapper;

	private double doubleValue = 2.223d;
	private float floatValue = 3.1415f;

	@Before
	public void setUp() {
		SystemContextHolder.setUserId(1 + "");
		SystemContextHolder.setTenantId(1 + "");
		SystemContextHolder.setAccountId(1 + "");
	}

	@Test
	public void testInsert() {
		LongIdPO insert = insert();
		LongIdPO po = longIdPOMapper.get(insert.getId());
		Assert.assertEquals((long) po.getCreateBy(), 1L);
		Assert.assertEquals((long) po.getVersion(), 0L);
		Assert.assertEquals(po.getProp1(), "stringProp1");
		Assert.assertEquals((int) po.getProp2(), Integer.MAX_VALUE);
		Assert.assertEquals(po.getProp3(), doubleValue, 3);
		Assert.assertEquals(po.getProp4(), floatValue, 4);
		Assert.assertEquals(po.getProp5(), Long.MAX_VALUE);
		Assert.assertTrue(po.getProp6().equals(BigInteger.valueOf(Long.MAX_VALUE)));
		Assert.assertEquals(po.getProp7().longValue(), Long.MAX_VALUE);
		Assert.assertEquals(po.getProp12(), Boolean.TRUE);
		Assert.assertEquals((byte) po.getProp13(), (byte) 4);
		Assert.assertEquals(po.getProp14(), 'c');
		Assert.assertEquals(po.getProp15(), (byte) 1);
		Assert.assertEquals(po.getProp16(), 128);
		Assert.assertEquals(po.getProp17(), Long.MAX_VALUE);
		Assert.assertEquals(po.getProp18(), doubleValue, 3);
		Assert.assertEquals(po.getProp19(), floatValue, 4);
		Assert.assertEquals(po.isProp20(), Boolean.TRUE);
		Assert.assertEquals(po.getProp21(), (byte) 2);
	}

	@Test
	public void testInsertNonNull() {
		LongIdPO insert = new LongIdPO();
		insert.setProp1("stringProp1");
		insert.setProp2(Integer.MAX_VALUE);
		insert.setProp3(Double.MAX_VALUE);
		insert.setProp4(2.3334f);
		longIdPOMapper.insertNonNull(insert);
		LongIdPO po = longIdPOMapper.get(insert.getId());
		Assert.assertEquals((long) po.getCreateBy(), 1L);
		Assert.assertEquals((long) po.getVersion(), 0L);
		Assert.assertEquals(po.getProp1(), "stringProp1");
		Assert.assertEquals((int) po.getProp2(), Integer.MAX_VALUE);
		Assert.assertEquals(po.getProp5(), 0L);
		Assert.assertEquals(po.getProp4(), 2.3334f,5);
		Assert.assertNull(po.getProp6());
		Assert.assertNull(po.getProp12());
	}


	@Test
	public void testBatchInsert() {
		List<LongIdPO> poList = batchList();
		long start = System.currentTimeMillis();
		int insert = longIdPOMapper.batchInsert(poList);
		long end = System.currentTimeMillis();
		System.out.println("batch insert :" + poList.size() + " rows cost:[" + (end - start) + "] millseconds");
		Assert.assertEquals(insert, poList.size());
	}

	private LongIdPO insert() {
		LongIdPO po = new LongIdPO();
		po.setProp1("stringProp1");
		po.setProp2(Integer.MAX_VALUE);
		po.setProp3(doubleValue);
		po.setProp4(floatValue);
		po.setProp5(Long.MAX_VALUE);
		po.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
		po.setProp7(BigDecimal.valueOf(Long.MAX_VALUE));
		po.setProp8(new Date());
		po.setProp9(new java.sql.Date(System.currentTimeMillis()));
		po.setProp10(LocalDate.now());
		po.setProp11(LocalDateTime.now());
		po.setProp12(Boolean.TRUE);
		po.setProp13((byte) 4);
		po.setProp14('c');
		po.setProp15((byte) 1);
		po.setProp16(128);
		po.setProp17(Long.MAX_VALUE);
		po.setProp18(doubleValue);
		po.setProp19(floatValue);
		po.setProp20(true);
		po.setProp21((short) 2);
		longIdPOMapper.insert(po);
		return po;
	}

	@Test
	public void testBatchUpdate() {
		List<LongIdPO> poList = batchList();
		longIdPOMapper.batchInsert(poList);
		for (LongIdPO longIdPO : poList) {
			longIdPO.setProp1("updateProperty");
			longIdPO.setProp2(Integer.MIN_VALUE);
		}
		longIdPOMapper.batchUpdate(poList);
		List<LongIdPO> updateList = longIdPOMapper.find(Criteria.from(LongIdPO.class).and(LongIdPO::getId).in(poList.stream().map(LongIdPO::getId).toArray(Long[]::new)));
		Assert.assertEquals(poList.size(), updateList.size());
		for (LongIdPO longIdPO : updateList) {
			Assert.assertEquals(longIdPO.getProp1(), "updateProperty");
			Assert.assertEquals((int) longIdPO.getProp2(), Integer.MIN_VALUE);
		}
	}

	@Test
	public void testBatchDelete() {
		List<LongIdPO> poList = batchList();
		longIdPOMapper.batchInsert(poList);
		for (LongIdPO po : poList) {
			longIdPOMapper.delete(po.getId());
		}
		List<LongIdPO> list = longIdPOMapper.find(Criteria.from(LongIdPO.class));
		Assert.assertEquals(list.size(), 0);
	}

	@Test
	public void testLogicDelete() {
		LongIdIsDeletedPO po = new LongIdIsDeletedPO();
		po.setProp1("LongIdIsDeletedPO");
		po.setProp2(Integer.MIN_VALUE);
		po.setProp2(Integer.MAX_VALUE);
		po.setProp3(Double.MAX_VALUE);
		po.setProp4(1.23f);
		po.setProp5(Long.MAX_VALUE);
		po.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
		po.setProp7(BigDecimal.valueOf(Integer.MAX_VALUE));
		po.setProp8(new Date());
		po.setProp9(new java.sql.Date(System.currentTimeMillis()));
		po.setProp10(LocalDate.now());
		po.setProp11(LocalDateTime.now());
		po.setProp12(Boolean.TRUE);
		po.setProp13((byte) 1);
		longIdIsDeletedPOMapper.insert(po);
		longIdIsDeletedPOMapper.delete(po.getId());
		LongIdIsDeletedPO isDeletedPO = longIdIsDeletedPOMapper.get(po.getId());
		Assert.assertNull(isDeletedPO);
	}

	private List<LongIdPO> batchList() {
		int count = 1000;
		List<LongIdPO> poList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			LongIdPO po = new LongIdPO();
			po.setProp1("stringProp1");
			po.setProp2(Integer.MAX_VALUE);
			po.setProp3(Double.MAX_VALUE);
			po.setProp4(1.23f);
			po.setProp5(Long.MAX_VALUE);
			po.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
			po.setProp7(BigDecimal.valueOf(Integer.MAX_VALUE));
			po.setProp8(new Date());
			po.setProp9(new java.sql.Date(System.currentTimeMillis()));
			po.setProp10(LocalDate.now());
			po.setProp11(LocalDateTime.now());
			po.setProp12(Boolean.TRUE);
			po.setProp13((byte) 1);
			poList.add(po);
		}
		return poList;
	}

	@After
	public void tearDown() throws Exception {
		longIdPOMapper.execute("truncate table t_test_long_id_po", null);
		longIdPOMapper.execute("truncate table t_test_long_id_is_deleted_po", null);
	}
}
