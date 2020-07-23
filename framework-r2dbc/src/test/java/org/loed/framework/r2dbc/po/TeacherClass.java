package org.loed.framework.r2dbc.po;


import org.loed.framework.common.po.BasePO;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/10/26 9:43 AM
 */
@Table(name = "t_test_teacher_class")
public class TeacherClass extends BasePO {
	@Column(length = 32)
	private String classId;
	@ManyToOne()
	@JoinColumn(name = "class_id")
	private Class aClass;
	@Column(length = 32)
	private String teacherId;
	@ManyToOne()
	@JoinColumn(name = "teacher_id")
	private Teacher teacher;

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public Class getaClass() {
		return aClass;
	}

	public void setaClass(Class aClass) {
		this.aClass = aClass;
	}

	public String getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
}
