package org.loed.framework.jdbc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.jdbc.CleverRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015/2/21 11:56
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pers/yangtao/framework/jdbc/spring-datasource.xml"})
public class CleverRowMapperTest {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testSingleColumn() throws Exception {
		String sql = "select count(*)  from t_uc_user";
		Long aLong = jdbcTemplate.queryForObject(sql, new CleverRowMapper<Long>(Long.class));
		System.out.println(aLong);
	}

	@Test
	public void testObject1() throws Exception {
		String sql = "select * from t_uc_user limit 2";
		List<User> userList = jdbcTemplate.query(sql, new CleverRowMapper<User>(User.class));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (User user : userList) {
			System.out.println("User{" +
					"name='" + user.getName() + '\'' +
					", namePath='" + user.getNaMePaTh() + '\'' +
					", userId='" + user.getUserId() + '\'' +
					", userName='" + user.getUserName() + '\'' +
					", createTime='" + sdf.format(user.getCreateTime()) + '\'' +
					", updateTime='" + sdf.format(user.getUpdateTime()) + '\'' +
					'}');
		}
	}

	@Test
	public void testObject2() throws Exception {
		String sql = "select *,name_path as \"organize.name_path\",name_path as \"orgMap.name_path\" from t_uc_user limit 2";
		List<User> userList = jdbcTemplate.query(sql, new CleverRowMapper<User>(User.class));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (User user : userList) {
			System.out.println("User{" +
					"name='" + user.getName() + '\'' +
					", namePath='" + user.getNaMePaTh() + '\'' +
					", orgNamePath='" + user.getOrganize().getNamePath() + '\'' +
					", orgMapNamePath='" + user.getOrgMap().get("name_path") + '\'' +
					", userId='" + user.getUserId() + '\'' +
					", userName='" + user.getUserName() + '\'' +
					", createTime='" + sdf.format(user.getCreateTime()) + '\'' +
					", updateTime='" + sdf.format(user.getUpdateTime()) + '\'' +
					'}');
		}
	}
}
