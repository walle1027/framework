package org.loed.framework.r2dbc.po;


import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/7/12 下午2:36
 */
@Table(name = "t_test_class")
public class Class extends CommonPO {
	@Column
	private String name;
	@Column
	private String grade;
	@OneToMany(targetEntity = Student.class)
	private List<Student> studentList;
	@OneToMany
	private List<TeacherClass> teachers;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public List<Student> getStudentList() {
		return studentList;
	}

	public void setStudentList(List<Student> studentList) {
		this.studentList = studentList;
	}

	public List<TeacherClass> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<TeacherClass> teachers) {
		this.teachers = teachers;
	}
}
