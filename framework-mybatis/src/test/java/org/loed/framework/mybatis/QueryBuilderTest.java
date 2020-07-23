package org.loed.framework.mybatis;

import org.junit.Test;
import org.loed.framework.mybatis.QueryBuilder;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/24 12:19 PM
 */
public class QueryBuilderTest {
	QueryBuilder queryBuilder = new QueryBuilder();

	@Test
	public void testSelect() {
		queryBuilder.select("id", "no", "name", "sex", "birthday");
		queryBuilder.from("t_student s");
		queryBuilder.leftJoin("t_teacher t on s.teacher_id = t.id");
		queryBuilder.innerJoin("t_grade g on s.grade_id = g.id");
		queryBuilder.where(" and s.id = ?");
		queryBuilder.where(" AND s.name like ?");
		System.out.println(queryBuilder.toString());
	}

	@Test
	public void testSelect2() {
		queryBuilder.from("t_student s");
		queryBuilder.leftJoin("t_teacher t on s.teacher_id = t.id");
		queryBuilder.select("id", "no", "name", "sex", "birthday");
		queryBuilder.select("g.grade_name");
		queryBuilder.select("t.teacher_name");
		queryBuilder.innerJoin("t_grade g on s.grade_id = g.id");
		queryBuilder.where("(");
		queryBuilder.where(" s.id = ?");
		queryBuilder.where(" or s.name like ?");
		queryBuilder.where(")");
		System.out.println(queryBuilder.toString());
	}

	@Test
	public void testSelect3() {
		queryBuilder.from("t_student s");
		queryBuilder.leftJoin("t_teacher t on s.teacher_id = t.id");
		queryBuilder.select("id", "no", "name", "sex", "birthday");
		queryBuilder.select("g.grade_name");
		queryBuilder.select("t.teacher_name");
		queryBuilder.innerJoin("t_grade g on s.grade_id = g.id");
		queryBuilder.where("t.name like ?");
		queryBuilder.where("and (");
		queryBuilder.where("and s.id != null");
		queryBuilder.where(" or s.name != ''");
		queryBuilder.where(")");
		queryBuilder.orderBy("s.name desc");
		queryBuilder.orderBy("s.birthday asc");
		System.out.println(queryBuilder.toString());
	}

	@Test
	public void testUpdate() {
		queryBuilder.update("t_student").set("version=version+1")
				.set("create_time=current_timestamp")
				.where("id=#{id}")
				.where("is_deleted = 0");
		System.out.println(queryBuilder.toString());
	}

}
