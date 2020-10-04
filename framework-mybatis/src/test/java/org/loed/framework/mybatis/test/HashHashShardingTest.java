package org.loed.framework.mybatis.test;

import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.query.Pagination;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.mybatis.test.mapper.ShardingPOMapper;
import org.loed.framework.mybatis.test.po.CommonPO;
import org.loed.framework.mybatis.test.po.ShardingPO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/25 4:51 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MybatisTestApplication.class)
public class HashHashShardingTest {
	@Autowired
	private ShardingPOMapper shardingPOMapper;

	@Test
	public void testInsert() {
		insert();
	}

	private ShardingPO insert() {
		ShardingPO shardingPO = new ShardingPO();
		shardingPO.setProp1(UUIDUtils.getUUID());
		shardingPO.setProp2(Integer.MAX_VALUE);
		shardingPO.setProp3(Double.MIN_VALUE);
		shardingPO.setProp4((float) 1023.123);
		shardingPO.setProp5(Long.MAX_VALUE);
		shardingPO.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
		shardingPO.setProp7(BigDecimal.ZERO);
		shardingPO.setProp8(new Date());
		shardingPO.setProp9(new java.sql.Date(System.currentTimeMillis()));
		shardingPO.setProp10(LocalDate.now());
		shardingPO.setProp11(LocalDateTime.now());
		shardingPO.setProp12(Boolean.TRUE);
		shardingPO.setProp13((byte) 0);
		shardingPOMapper.insert(shardingPO);
		return shardingPO;
	}

	@Test
	public void testBatchInsert() {
		List<ShardingPO> shardingPOList = shardingPOList();
		shardingPOMapper.batchInsert(shardingPOList);
	}

	@Test
	public void testUpdate() {
		ShardingPO insert = insert();
		insert.setProp2(Integer.MIN_VALUE);
		int update = shardingPOMapper.update(insert);
		Assert.assertEquals(update, 1);
		ShardingPO po = shardingPOMapper.get(insert.getId());
		Assert.assertEquals((int) po.getProp2(), Integer.MIN_VALUE);
	}

	@Test
	public void testBatchUpdate() {
		List<ShardingPO> shardingPOList = shardingPOList();
		shardingPOMapper.batchInsert(shardingPOList);
		for (ShardingPO shardingPO : shardingPOList) {
			shardingPO.setProp2(Integer.MIN_VALUE);
		}
		shardingPOMapper.batchUpdate(shardingPOList);

		Criteria<ShardingPO> criteria = Criteria.from(ShardingPO.class).and(ShardingPO::getId).in(shardingPOList.stream().map(ShardingPO::getId).collect(Collectors.toList()));

		List<ShardingPO> updateList = shardingPOMapper.find(criteria);

		Assert.assertEquals(shardingPOList.size(), updateList.size());
		for (ShardingPO shardingPO : updateList) {
			Assert.assertEquals((int) shardingPO.getProp2(), Integer.MIN_VALUE);
		}

		Criteria<ShardingPO> criteria2 = Criteria.from(ShardingPO.class).and(ShardingPO::getProp1).in(shardingPOList.stream().map(ShardingPO::getProp1).collect(Collectors.toList()));

		List<ShardingPO> updateList2 = shardingPOMapper.find(criteria2);

		Assert.assertEquals(shardingPOList.size(), updateList2.size());
		for (ShardingPO shardingPO : updateList2) {
			Assert.assertEquals((int) shardingPO.getProp2(), Integer.MIN_VALUE);
		}
	}

	@Test
	public void testDelete() {
		ShardingPO insert = insert();
		shardingPOMapper.delete(insert.getId());
		ShardingPO po = shardingPOMapper.get(insert.getId());
		Assert.assertNull(po);
	}

	@Test
	public void testDeleteByCriteria() {
		List<ShardingPO> poList = shardingPOList();
		shardingPOMapper.batchInsert(poList);
		Criteria<ShardingPO> criteria = Criteria.from(ShardingPO.class).and(ShardingPO::getId).in(poList.stream().map(ShardingPO::getId).toArray(String[]::new))
				.and(ShardingPO::getProp2).greaterThan(0);
		shardingPOMapper.delete(criteria);

		List<ShardingPO> list = shardingPOMapper.find(criteria);
		Assert.assertEquals(list.size(), 0);
	}

	@Test
	public void testFind() {
		List<ShardingPO> shardingPOList = shardingPOList();
		shardingPOMapper.batchInsert(shardingPOList);
		Criteria<ShardingPO> criteria = Criteria.from(ShardingPO.class).and(ShardingPO::getProp1).in(shardingPOList.stream().map(ShardingPO::getProp1).toArray(String[]::new))
				.and(ShardingPO::getProp2).greaterThan(0)
				.and(ShardingPO::getProp6).greaterEqual(BigInteger.valueOf(Long.MIN_VALUE));
		List<ShardingPO> poList = shardingPOMapper.find(criteria);
		Assert.assertEquals(poList.size(), shardingPOList.size());
		String prop1 = shardingPOList.get(0).getProp1();
		ShardingPO po = shardingPOMapper.findOne(Criteria.from(ShardingPO.class).and(ShardingPO::getProp1).is(shardingPOList.get(0).getProp1()));
		Assert.assertEquals(po.getProp1(), prop1);
		Assert.assertEquals(shardingPOList.get(0).getId(), po.getId());
	}

	@Test
	public void testFindPage() {
		List<ShardingPO> shardingPOList = shardingPOList();
		shardingPOMapper.batchInsert(shardingPOList);
		Criteria<ShardingPO> criteria = Criteria.from(ShardingPO.class).and(ShardingPO::getProp1).in(shardingPOList.stream().map(ShardingPO::getProp1).collect(Collectors.toList()))
				.and(ShardingPO::getProp2).greaterThan(0)
				.and(ShardingPO::getProp6).greaterEqual(BigInteger.valueOf(Long.MIN_VALUE));
		Pagination<ShardingPO> page = shardingPOMapper.findPage(PageRequest.of(0, 10), criteria);
		Assert.assertEquals(page.getRows().size(), 10);
		Assert.assertEquals(page.getTotal(), shardingPOList.size());
	}

	@Test
	public void testFindByProperty() {
		ShardingPO insert = insert();
		List<ShardingPO> poList = shardingPOMapper.find(ShardingPO::getProp1, insert.getProp1());
		Assert.assertEquals(poList.size(), 1);
		ShardingPO shardingPO = shardingPOMapper.findOne(ShardingPO::getProp1, insert.getProp1());
		Assert.assertEquals(shardingPO.getId(), insert.getId());
	}

	@Test
	public void testCount() {
		List<ShardingPO> shardingPOS = shardingPOList();
		shardingPOMapper.batchInsert(shardingPOS);
		Criteria<ShardingPO> criteria = Criteria.from(ShardingPO.class).and(CommonPO::getId).in(shardingPOS.stream().map(ShardingPO::getId).collect(Collectors.toList()));
		Long count = shardingPOMapper.count(criteria);
		Assert.assertEquals(shardingPOS.size(), count.intValue());
	}

	private List<ShardingPO> shardingPOList() {
		int count = 1000;
		List<ShardingPO> shardingPOList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ShardingPO shardingPO = new ShardingPO();
			shardingPO.setProp1(UUIDUtils.getUUID());
			shardingPO.setProp2(Integer.MAX_VALUE);
			shardingPO.setProp3(Double.MIN_VALUE);
			shardingPO.setProp4(1.23f);
			shardingPO.setProp5(Long.MAX_VALUE);
			shardingPO.setProp6(BigInteger.valueOf(Long.MAX_VALUE));
			shardingPO.setProp7(BigDecimal.ZERO);
			shardingPO.setProp8(new Date());
			shardingPO.setProp9(new java.sql.Date(System.currentTimeMillis()));
			shardingPO.setProp10(LocalDate.now());
			shardingPO.setProp11(LocalDateTime.now());
			shardingPO.setProp12(Boolean.TRUE);
			shardingPO.setProp13((byte) 0);
			shardingPOList.add(shardingPO);
		}
		return shardingPOList;
	}

	@After
	public void tearDown() throws Exception {
		shardingPOMapper.execute("truncate table t_test_sharding_po_1",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_2",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_3",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_4",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_5",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_6",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_7",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_8",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_9",null);
		shardingPOMapper.execute("truncate table t_test_sharding_po_10",null);
		shardingPOMapper.execute("truncate table t_id_mapping",null);
		shardingPOMapper.execute("truncate table t_sharding_mapping",null);
	}
}
