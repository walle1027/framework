package org.loed.framework;

import org.junit.Test;
import org.loed.framework.common.query.Criteria;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.translate.Student;
import org.loed.framework.translate.Stuff;
import org.loed.framework.translate.Teacher;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/22 5:21 下午
 */
public class CriteriaTest {
	@Test
	public void testFluent() {
		Criteria<Student> criteria = Criteria.from(Student.class);
		criteria.and(Student::getAge).greaterThan(13)
				.and(Student::getName).is("张三")
				.or(Student::getCode).isNot("zhangsan");
		criteria.left(Student::getTeacher).inner(Teacher::getStuff).and(Stuff::getName).is("李");
		System.out.println(SerializeUtils.toJson(criteria));
	}
}
