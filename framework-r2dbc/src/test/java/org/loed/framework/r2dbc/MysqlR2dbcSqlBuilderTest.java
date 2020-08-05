package org.loed.framework.r2dbc;

import org.junit.Before;
import org.junit.Test;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.query.Condition;
import org.loed.framework.common.query.Operator;
import org.loed.framework.common.util.LocalDateUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.r2dbc.dao.R2dbcParam;
import org.loed.framework.r2dbc.dao.R2dbcQuery;
import org.loed.framework.r2dbc.dao.dialect.MysqlR2dbcSqlBuilder;
import org.loed.framework.r2dbc.po.Person;
import org.loed.framework.r2dbc.po.Sex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/5 9:53 上午
 */
public class MysqlR2dbcSqlBuilderTest {
	private MysqlR2dbcSqlBuilder mysqlR2dbcSqlBuilder = new MysqlR2dbcSqlBuilder(true);
	private Person person;

	@Before
	public void setUp() throws Exception {
		person = new Person();
		person.setId(UUIDUtils.getUUID());
		person.setVersion(0L);
		person.setCreateTime(LocalDateUtils.convertDateToLDT(new Date()));
		person.setCreateBy("MysqlR2dbcSqlBuilderTest");
		person.setUpdateTime(LocalDateUtils.convertDateToLDT(new Date()));
		person.setUpdateBy("MysqlR2dbcSqlBuilderTest");
		person.setIsDeleted((byte) 0);
		person.setName("MysqlR2dbcSqlBuilderTest#name");
		person.setBirthday(LocalDateUtils.convertDateToLDT(new Date()));
		person.setSex(Sex.Female);
	}

	@Test
	public void testInsert() {
		R2dbcQuery query = mysqlR2dbcSqlBuilder.insert(person, ORMapping.get(Person.class));
		printQuery(query);
	}

	@Test
	public void testUpdate() {
		List<Condition> conditionList = new ArrayList<>();
		conditionList.add(new Condition("id", Operator.equal, person.getId()));
		conditionList.add(new Condition("isDeleted", Operator.equal, (byte) 0));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCondition(person, ORMapping.get(Person.class), conditionList, null, null);
		printQuery(query);
	}

	@Test
	public void testUpdateWith() {
		List<Condition> conditionList = new ArrayList<>();
		conditionList.add(new Condition("id", Operator.equal, person.getId()));
		conditionList.add(new Condition("isDeleted", Operator.equal, (byte) 0));
		List<String> includes = new ArrayList<>();
		includes.add(LambdaUtils.getPropFromLambda(Person::getName));
		includes.add(LambdaUtils.getPropFromLambda(Person::getBirthday));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCondition(person, ORMapping.get(Person.class), conditionList, includes, null);
		printQuery(query);
	}

	@Test
	public void testUpdateWithout() {
		List<Condition> conditionList = new ArrayList<>();
		conditionList.add(new Condition("id", Operator.equal, person.getId()));
		conditionList.add(new Condition("isDeleted", Operator.equal, (byte) 0));
		List<String> excludes = new ArrayList<>();
		excludes.add(LambdaUtils.getPropFromLambda(Person::getSex));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCondition(person, ORMapping.get(Person.class), conditionList, null, excludes);
		printQuery(query);
	}

	@Test
	public void testUpdateWithAndWithout() {
		List<Condition> conditionList = new ArrayList<>();
		conditionList.add(new Condition("id", Operator.equal, person.getId()));
		conditionList.add(new Condition("isDeleted", Operator.equal, (byte) 0));
		List<String> includes = new ArrayList<>();
		includes.add(LambdaUtils.getPropFromLambda(Person::getName));
		includes.add(LambdaUtils.getPropFromLambda(Person::getBirthday));
		List<String> excludes = new ArrayList<>();
		excludes.add(LambdaUtils.getPropFromLambda(Person::getSex));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCondition(person, ORMapping.get(Person.class), conditionList, includes, excludes);
		printQuery(query);
	}

	private void printQuery(R2dbcQuery query) {
		System.out.println(query.getStatement());
		Map<String, R2dbcParam> params = query.getParams();
		if (params != null) {
			for (Map.Entry<String, R2dbcParam> entry : params.entrySet()) {
				System.out.println(entry.getKey() + "=" + entry.getValue());
			}
		}
	}
}
