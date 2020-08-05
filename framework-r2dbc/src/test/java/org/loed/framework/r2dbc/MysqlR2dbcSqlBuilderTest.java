package org.loed.framework.r2dbc;

import org.junit.Before;
import org.junit.Test;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.lambda.LambdaUtils;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.LocalDateUtils;
import org.loed.framework.common.util.UUIDUtils;
import org.loed.framework.r2dbc.dao.R2dbcParam;
import org.loed.framework.r2dbc.dao.R2dbcQuery;
import org.loed.framework.r2dbc.dao.R2dbcSqlBuilder;
import org.loed.framework.r2dbc.dao.dialect.MysqlR2dbcSqlBuilder;
import org.loed.framework.r2dbc.po.People;
import org.loed.framework.r2dbc.po.Person;
import org.loed.framework.r2dbc.po.Sex;

import java.util.*;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/5 9:53 上午
 */
public class MysqlR2dbcSqlBuilderTest {
	private MysqlR2dbcSqlBuilder mysqlR2dbcSqlBuilder = new MysqlR2dbcSqlBuilder(true);
	private Person person;

	private People people;

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

		people = new People();
		people.setCreateTime(LocalDateUtils.convertDateToLDT(new Date()));
		people.setUpdateTime(LocalDateUtils.convertDateToLDT(new Date()));
		people.setId(1L);
		people.setName("MysqlR2dbcSqlBuilderTest.People");
		people.setRace("race.han");
		people.setSex((byte) 0);
	}

	@Test
	public void testInsertWithAssignedId() {
		R2dbcQuery query = mysqlR2dbcSqlBuilder.insert(person, ORMapping.get(Person.class));
		printQuery(query);
	}

	@Test
	public void testInsertWithAutoIncreaseId() {
		R2dbcQuery autoGenIdInsert = mysqlR2dbcSqlBuilder.insert(people, ORMapping.get(People.class));
		printQuery(autoGenIdInsert);
	}

	@Test
	public void testBatchInsertWithAssignedId() {
		List<People> peopleList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			People person = new People();
			person.setVersion(0L);
			person.setCreateTime(LocalDateUtils.convertDateToLDT(new Date()));
			person.setUpdateTime(LocalDateUtils.convertDateToLDT(new Date()));
			person.setName("MysqlR2dbcSqlBuilderTest#testBatchInsert.name" + i);
			person.setSex((byte) (i % 2));
			peopleList.add(person);
		}
		R2dbcQuery query = mysqlR2dbcSqlBuilder.batchInsert(peopleList, ORMapping.get(People.class));
		printQuery(query);
	}

	@Test
	public void testBatchInsertWithAutoGeneratedId() {
		List<Person> personList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Person person = new Person();
			person.setId(UUIDUtils.getUUID());
			person.setVersion(0L);
			person.setCreateTime(LocalDateUtils.convertDateToLDT(new Date()));
			person.setCreateBy("MysqlR2dbcSqlBuilderTest#testBatchInsert" + i);
			person.setUpdateTime(LocalDateUtils.convertDateToLDT(new Date()));
			person.setUpdateBy("MysqlR2dbcSqlBuilderTest#testBatchInsert" + i);
			person.setIsDeleted((byte) 0);
			person.setName("MysqlR2dbcSqlBuilderTest#testBatchInsert.name" + i);
			person.setBirthday(LocalDateUtils.convertDateToLDT(new Date()));
			person.setSex(Sex.Female);
			personList.add(person);
		}
		R2dbcQuery query = mysqlR2dbcSqlBuilder.batchInsert(personList, ORMapping.get(Person.class));
		printQuery(query);
	}

	@Test
	public void testUpdate() {
		Criteria<Person> criteria = Criteria.from(Person.class).and(Person::getId).is(person.getId())
				.and(Person::getIsDeleted).is((byte) 0);
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCriteria(person, ORMapping.get(Person.class), criteria, R2dbcSqlBuilder.ALWAYS_TRUE_FILTER);
		printQuery(query);
	}

	@Test
	public void testUpdateWith() {
		Criteria<Person> criteria = Criteria.from(Person.class).and(Person::getId).is(person.getId())
				.and(Person::getIsDeleted).is((byte) 0);
		List<String> includes = new ArrayList<>();
		includes.add(LambdaUtils.getPropFromLambda(Person::getName));
		includes.add(LambdaUtils.getPropFromLambda(Person::getBirthday));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCriteria(person, ORMapping.get(Person.class), criteria, new R2dbcSqlBuilder.IncludeFilter(includes));
		printQuery(query);
	}

	@Test
	public void testUpdateWithout() {
		Criteria<Person> criteria = Criteria.from(Person.class).and(Person::getId).is(person.getId())
				.and(Person::getIsDeleted).is((byte) 0);
		List<String> excludes = new ArrayList<>();
		excludes.add(LambdaUtils.getPropFromLambda(Person::getSex));
		R2dbcQuery query = mysqlR2dbcSqlBuilder.updateByCriteria(person, ORMapping.get(Person.class), criteria, new R2dbcSqlBuilder.ExcludeFilter(excludes));
		printQuery(query);
	}

	@Test
	public void testDelete() {
		Table table = ORMapping.get(People.class);
		R2dbcQuery query = mysqlR2dbcSqlBuilder.deleteByCriteria(table, Criteria.from(People.class).and(People::getId).is(1L)
				.and(People::getVersion).is(0L));
		printQuery(query);
	}

	@Test
	public void testLogicDelete() {
		Table table = ORMapping.get(Person.class);
		R2dbcQuery query = mysqlR2dbcSqlBuilder.deleteByCriteria(table, Criteria.from(Person.class).and(Person::getId).is(1L)
				.and(Person::getVersion).is(0L));
		printQuery(query);
	}

	@Test
	public void testFindByCriteria() {
		Table table = ORMapping.get(Person.class);
		Criteria<Person> criteria = Criteria.from(Person.class);
		List<Sex> sexes = new ArrayList<>();
		sexes.add(Sex.Female);
		sexes.add(Sex.Male);
		criteria.and(Person::getId).in(Arrays.asList("1", "2"))
				.and(Person::getName).beginWith("test")
				.and(Person::getName).notBeginWith("test")
				.and(Person::getName).endWith("test")
				.and(Person::getName).notEndWith("test")
				.and(Person::getName).contains("test")
				.and(Person::getName).notContains("test")
				.and(Person::getAge).between(8, 80)
				.and(Person::getAge).notBetween(6, 90)
				.and(Person::getSex).blank()
				.and(Person::getSex).notBlank()
				.and(Person::getSex).is("test")
				.and(Person::getSex).isNot("test")
				.and(Person::getAge).greaterThan(10)
				.and(Person::getAge).greaterEqual(10)
				.and(Person::getAge).lessEqual(8)
				.and(Person::getAge).lessThan(8)
				.and(Person::getSex).isNull()
				.and(Person::getSex).isNotNull()
				.and(Person::getSex).in(sexes)
				.and(Person::getAge).in(new int[]{1, 2, 3, 4, 5})
				.and(Person::getAge).in(new long[]{1L, 2L, 3L, 4L, 5L})
				.and(Person::getAge).in(new short[]{1, 2, 3, 4, 5})
				.and(Person::getAge).in(new char[]{'1', '2', '3', '4', '5'})
				.and(Person::getAge).in(new double[]{1D, 2D, 3D, 4D, 5D})
				.and(Person::getAge).in(new byte[]{1, 2, 3, 4, 5})
				.and(Person::getAge).in(new float[]{1, 2, 3, 4, 5})
				.and(Person::getAge).in(new boolean[]{true, false})
				.and(Person::getAge).in(new Object[]{1, 2, 3, 4, 5})
				.and(Person::getName).custom(" > age");
		R2dbcQuery query = mysqlR2dbcSqlBuilder.findByCriteria(table, criteria);
		printQuery(query);
	}

	private void printQuery(R2dbcQuery query) {
		String statement = query.getStatement();
		System.out.println("raw statement:");
		System.out.println(statement);
		System.out.println("raw params:");
		Map<String, R2dbcParam> params = query.getParams();
		if (params != null) {
			for (Map.Entry<String, R2dbcParam> entry : params.entrySet()) {
				String key = entry.getKey();
				R2dbcParam value = entry.getValue();
				if (value.getParamValue() == null) {
					statement = statement.replaceAll(":" + key + "\\s", "null");
				} else {
					statement = statement.replaceAll(":" + key + "\\s", "'" + value.getParamValue().toString() + "'");
				}
				System.out.println(key + "=" + value);
			}
		}
		System.out.println("formatted statement:");
		System.out.println(statement);
	}
}
