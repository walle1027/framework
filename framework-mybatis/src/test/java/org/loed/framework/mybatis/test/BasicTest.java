package org.loed.framework.mybatis.test;

import org.loed.framework.common.context.SystemContextHolder;
import org.loed.framework.common.query.*;
import org.loed.framework.mybatis.test.mapper.UserMapper;
import org.loed.framework.mybatis.test.po.User;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/22 1:48 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MybatisTestApplication.class)
public class BasicTest {
	@Autowired
	private UserMapper userMapper;

	@Before
	public void setUp() throws Exception {
		SystemContextHolder.setTenantCode("mybatis_test_tenant");
		SystemContextHolder.setAccountId("mybatis_test_accountId");
		SystemContextHolder.setUserId("mybatis_test_userId");
	}

	@Test
	public void testInsert() {
		insert();
	}

	@Test
	public void testBatchInsert() {
		List<User> userList = new ArrayList<>();
		int batchCount = 10000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < batchCount; i++) {
			User user = new User();
			user.setAccount("testAccount" + i);
			user.setEmail("test@test.com" + i);
			user.setExpireAt(DateUtils.addDays(new Date(), 100));
			user.setIsLocked(0);
			user.setMobile("testMobile" + i);
			user.setPassword("testPassword" + i);
			user.setUsername("testUsername" + i);
			userList.add(user);
		}

		int i = userMapper.batchInsert(userList);
		Assert.assertEquals(i, batchCount);
		long end = System.currentTimeMillis();
		System.out.println("batch insert:" + batchCount + " rows cost:" + (end - start) + " millseconds");
	}

	@Test
	public void testUpdate() {
		User insert = insert();
		String userId = insert.getId();
		User user = new User();
		user.setUsername("testUpdate");
		user.setPassword("testUpdate");
		user.setMobile("testUpdate");
		user.setIsLocked(1);
		user.setAccount("testUpdate");
		user.setId(userId);
		userMapper.update(user);
		User userPO = userMapper.get(userId);
		Assert.assertEquals(userPO.getUsername(), "testUpdate");
		Assert.assertEquals(userPO.getPassword(), "testUpdate");
		Assert.assertEquals(userPO.getMobile(), "testUpdate");
		Assert.assertEquals(userPO.getAccount(), "testUpdate");
		Assert.assertSame(userPO.getIsLocked(), 1);
		Assert.assertEquals(0, (byte) userPO.getIsDeleted());
		Assert.assertEquals(userPO.getCreateBy(), SystemContextHolder.getUserId());
	}

	@Test
	public void testUpdateNonBlank() {
		User insert = insert();
		String userId = insert.getId();
		User user = new User();
		user.setUsername("testUpdate");
		user.setId(userId);
		userMapper.updateNonBlank(user);
		User userPO = userMapper.get(userId);
		Assert.assertEquals(userPO.getAccount(), "testAccount");
		Assert.assertEquals(userPO.getUsername(), "testUpdate");
		Assert.assertSame(userPO.getVersion(), 1L);
	}

	@Test
	public void testUpdateNonBlankAnd() {
		User insert = insert();
		String userId = insert.getId();
		User user = new User();
		user.setUsername("testUpdate");
		user.setId(userId);
		userMapper.updateNonBlankAnd(user, User::getAccount);
		User userPO = userMapper.get(userId);
		Assert.assertNull(userPO.getAccount());
		Assert.assertEquals(userPO.getUsername(), "testUpdate");
		Assert.assertSame(userPO.getVersion(), 1L);
	}


	@Test
	public void testUpdateWith() {
		User insert = insert();
		String userId = insert.getId();
		User user = new User();
		user.setUsername("testUpdate");
		user.setId(userId);
		userMapper.updateWith(user, User::getUsername, User::getAccount);
		User userPO = userMapper.get(userId);
		Assert.assertNull(userPO.getAccount());
		Assert.assertEquals(userPO.getUsername(), "testUpdate");
		Assert.assertSame(userPO.getVersion(), 1L);
		Assert.assertEquals((byte) 0, (byte) userPO.getIsDeleted());
	}

	@Test
	public void testUpdateWithout() {
		User insert = insert();
		String userId = insert.getId();
		User user = new User();
		user.setUsername("testUpdate");
		user.setId(userId);
		userMapper.updateWithout(user, User::getAccount);
		User userPO = userMapper.get(userId);
		Assert.assertNull(userPO.getPassword());
		Assert.assertEquals("testUpdate", userPO.getUsername());
		Assert.assertEquals("testAccount", userPO.getAccount());
		Assert.assertSame(userPO.getVersion(), 1L);
		Assert.assertEquals((byte) 0, (byte) userPO.getIsDeleted());
	}

	@Test
	public void testBatchUpdate() {
		List<User> userList = batchInsert();

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			user.setUsername("testUpdate" + user.getId());
		}
		long batchUpdateStart = System.currentTimeMillis();
		userMapper.batchUpdate(userList);
		long batchUpdateEnd = System.currentTimeMillis();
		System.out.println("batch update {" + userList.size() + "} rows cost:[" + (batchUpdateEnd - batchUpdateStart) + "] millseconds");
		List<User> userPOList = userMapper.find(Criteria.from(User.class).and(User::getId).in(userList.stream().map(User::getId).collect(Collectors.toList())));
		for (User user : userPOList) {
			int idx = Integer.parseInt(user.getAccount().substring("testAccount".length()));
			Assert.assertEquals("testUpdate" + user.getId(), user.getUsername());
			Assert.assertEquals("testAccount" + idx, user.getAccount());
			Assert.assertEquals("testMobile" + idx, user.getMobile());
			Assert.assertEquals((byte) 0, (byte) user.getIsDeleted());
			Assert.assertEquals(1L, (long) user.getVersion());
		}
	}

	@Test
	public void testBatchUpdateWith() {
		List<User> userList = batchInsert();

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			user.setUsername("testUpdate" + user.getId());
		}
		long batchUpdateStart = System.currentTimeMillis();
		userMapper.batchUpdateWith(userList, User::getUsername);
		long batchUpdateEnd = System.currentTimeMillis();
		System.out.println("batch update {" + userList.size() + "} rows cost:[" + (batchUpdateEnd - batchUpdateStart) + "] millseconds");
		List<User> userPOList = userMapper.find(Criteria.from(User.class).and(User::getId).in(userList.stream().map(User::getId).collect(Collectors.toList())));
		for (User user : userPOList) {
			Assert.assertEquals("testUpdate" + user.getId(), user.getUsername());
			Assert.assertEquals((byte) 0, (byte) user.getIsDeleted());
			Assert.assertEquals(1L, (long) user.getVersion());
		}
	}

	@Test
	public void testBatchUpdateWithout() {
		List<User> userList = batchInsert();

		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			user.setUsername("testUpdate" + user.getId());
		}
		long batchUpdateStart = System.currentTimeMillis();
		userMapper.batchUpdateWithOut(userList, User::getUsername);
		long batchUpdateEnd = System.currentTimeMillis();
		System.out.println("batch update {" + userList.size() + "} rows cost:[" + (batchUpdateEnd - batchUpdateStart) + "] millseconds");
		List<User> userPOList = userMapper.find(Criteria.from(User.class).and(User::getId).in(userList.stream().map(User::getId).collect(Collectors.toList())));
		for (User user : userPOList) {
			int idx = Integer.parseInt(user.getAccount().substring("testAccount".length()));
			Assert.assertEquals("testUsername" + idx, user.getUsername());
			Assert.assertEquals((byte) 0, (byte) user.getIsDeleted());
			Assert.assertEquals(1L, (long) user.getVersion());
		}
	}

	@Test
	public void testBatchUpdateNonBlank() {
		List<User> userList = batchInsert();
		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			user.setUsername(null);
			user.setAccount(" ");
			user.setEmail("");
		}
		long batchUpdateStart = System.currentTimeMillis();
		userMapper.batchUpdateNonBlank(userList);
		long batchUpdateEnd = System.currentTimeMillis();
		System.out.println("batch update {" + userList.size() + "} rows cost:[" + (batchUpdateEnd - batchUpdateStart) + "] millseconds");
		List<User> userPOList = userMapper.find(Criteria.from(User.class).and(User::getId).in(userList.stream().map(User::getId).collect(Collectors.toList())));
		for (User user : userPOList) {
			int idx = Integer.parseInt(user.getAccount().substring("testAccount".length()));
			Assert.assertEquals("testUsername" + idx, user.getUsername());
			Assert.assertEquals("testAccount" + idx, user.getAccount());
			Assert.assertEquals("test@test.com" + idx, user.getEmail());
			Assert.assertEquals((byte) 0, (byte) user.getIsDeleted());
			Assert.assertEquals(1L, (long) user.getVersion());
		}
	}

	@Test
	public void testDelete() {
		User insert = insert();
		userMapper.delete(insert.getId());

		User user = userMapper.get(insert.getId());

		Assert.assertNull(user);
	}

	@Test
	public void testDeleteByCriteria() {
		List<User> userList = batchInsert();
		Criteria<User> criteria = Criteria.from(User.class).and(User::getUsername).beginWith("testUsername");
		userMapper.delete(criteria);
		List<User> users = userMapper.find(criteria);
		Assert.assertEquals(users.size(), 0);
	}

	@Test
	public void testGet() {
		User insert = insert();
		User user = userMapper.get(insert.getId());
		Assert.assertEquals(insert.getAccount(), user.getAccount());
		Assert.assertEquals(insert.getUsername(), user.getUsername());
		Assert.assertEquals(insert.getEmail(), user.getEmail());
		Assert.assertEquals(insert.getPassword(), user.getPassword());
		Assert.assertEquals((byte) 0, (byte) user.getIsDeleted());
		Assert.assertEquals((long) 0, (long) user.getVersion());
		Assert.assertEquals(SystemContextHolder.getUserId(), user.getCreateBy());
	}

	@Test
	public void testFindByProperty() {
		User insert = insert();
		List<User> users = userMapper.find(User::getUsername, "testUsername");
		users.forEach(user -> {
			Assert.assertEquals(user.getUsername(), "testUsername");
		});
	}

	@Test
	public void testFindOne() {
		User insert = insert();
		User user = userMapper.findOne(Criteria.from(User.class).and(User::getId).is(insert.getId()));
		Assert.assertEquals(user.getUsername(), insert.getUsername());
		Assert.assertEquals(user.getAccount(), insert.getAccount());
		Assert.assertEquals(user.getPassword(), insert.getPassword());
	}

	@Test
	public void testFindOne2() {
		User insert = insert();
		User user = userMapper.findOne(User::getId, insert.getId());
		Assert.assertEquals(user.getUsername(), insert.getUsername());
		Assert.assertEquals(user.getAccount(), insert.getAccount());
		Assert.assertEquals(user.getPassword(), insert.getPassword());
	}

	@Test
	public void testFindPage() {
		List<User> userList = batchInsert();
		Criteria<User> criteria = Criteria.from(User.class)
				.and(User::getUsername).beginWith("testUsername")
				.and(User::getAccount).notBeginWith("abc")
				.and(User::getEmail).contains("test.com")
				.and(User::getEmail).notContains("abc.com")
				.and(User::getMobile).endWith("1")
				.and(User::getMobile).notEndWith("abc.com")
				.and(User::getIsLocked).is(0)
				.and(User::getIsLocked).isNot(1)
				.and(User::getExpireAt).greaterEqual(DateUtils.addDays(new Date(), -1))
				.and(User::getExpireAt).greaterThan(DateUtils.addDays(new Date(), -1))
				.and(User::getExpireAt).lessEqual(new Date())
				.and(User::getExpireAt).lessThan(new Date())
				.and(User::getExpireAt).between(DateUtils.addDays(new Date(), -1), DateUtils.addDays(new Date(), 1))
				.and(User::getExpireAt).notBetween(DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), -1))
				.and(User::getIsLocked).in(new Integer[]{0, 1})
				.and(User::getIsLocked).notIn(new Integer[]{2, 3})
				.and(User::getIsLocked).in(Collections.singletonList(0))
				.and(User::getIsLocked).notIn(Collections.singleton(2))
				.and(User::getMobile).notBlank()
				.and(User::getUpdateBy).isNull()
				.and(User::getCreateBy).isNotNull()
				.and(User::getCreateBy).blank()
				.and(User::getIsLocked).custom(" > 0 ")
				.and(User::getIsDeleted).is((byte) 0);
		PageRequest pageRequest = PageRequest.of(0, 10);
		Pagination<User> pagination = userMapper.findPage(pageRequest, criteria);
		Assert.assertEquals(pagination.getRows().size(), 0);
		Assert.assertEquals(pageRequest.getPageNumber(), 0);
		Assert.assertEquals(pageRequest.getPageSize(), 10);
	}

	@Test
	public void testSubCondition() {
		List<User> userList = batchInsert();
		Criteria<User> criteria = Criteria.from(User.class);
		Condition condition = new Condition();
		List<Condition> subConditions = new ArrayList<>();
		subConditions.add(new Condition(Joint.or, User::getUsername, Operator.contains, "testAccount"));
		subConditions.add(new Condition(Joint.or, User::getAccount, Operator.contains, "testAccount"));
		subConditions.add(new Condition(Joint.or, User::getEmail, Operator.contains, "testAccount"));
		condition.setSubConditions(subConditions);
		criteria.getConditions().add(condition);
		List<User> users = userMapper.find(criteria);
		Assert.assertEquals(userList.size(), users.size());
		for (User user : users) {
			int i = Integer.parseInt(user.getAccount().substring("testAccount".length()));
			Assert.assertEquals("testUsername" + i, user.getUsername());
			Assert.assertEquals("test@test.com" + i, user.getEmail());
		}
	}

	@Test
	public void testCount() {
		List<User> users = batchInsert();
		Long count = userMapper.count(Criteria.from(User.class).and(User::getUsername).beginWith("testUsername"));
		Assert.assertEquals((long) count, users.size());
	}

	@Test
	public void checkRepeat() {
		boolean repeat1 = userMapper.checkRepeat(null, User::getAccount, "testAccount");
		Assert.assertFalse(repeat1);
		User insert = insert();
		boolean repeat2 = userMapper.checkRepeat(null, User::getAccount, "testAccount");
		Assert.assertTrue(repeat2);
		boolean repeat3 = userMapper.checkRepeat(insert.getId(), User::getAccount, "testAccount");
		Assert.assertFalse(repeat3);
	}

	private User insert() {
		User user = new User();
		user.setAccount("testAccount");
		user.setEmail("test@test.com");
		user.setExpireAt(DateUtils.addDays(new Date(), 100));
		user.setIsLocked(0);
		user.setMobile("testMobile");
		user.setPassword("testPassword");
		user.setUsername("testUsername");
		int i = userMapper.insert(user);
		Assert.assertEquals(i, 1);
		return user;
	}

	private List<User> batchInsert() {
		List<User> userList = new ArrayList<>();
		int batchCount = 100;
		for (int i = 0; i < batchCount; i++) {
			User user = new User();
			user.setAccount("testAccount" + i);
			user.setEmail("test@test.com" + i);
			user.setExpireAt(DateUtils.addDays(new Date(), 100));
			user.setIsLocked(0);
			user.setMobile("testMobile" + i);
			user.setPassword("testPassword" + i);
			user.setUsername("testUsername" + i);
			userList.add(user);
		}
		long batchInsertStart = System.currentTimeMillis();
		userMapper.batchInsert(userList);
		long batchInsertEnd = System.currentTimeMillis();

		System.out.println("batch insert{" + batchCount + "} rows cost:[" + (batchInsertEnd - batchInsertStart) + "] millseconds");
		return userList;
	}

	@After
	public void tearDown() throws Exception {
		//清空数据库
		userMapper.execute("truncate table t_user", null);
	}
}
