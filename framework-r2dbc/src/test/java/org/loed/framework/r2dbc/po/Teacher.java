package org.loed.framework.r2dbc.po;


import org.loed.framework.common.po.BasePO;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/10/26 9:42 AM
 */
@Table(name = "t_test_teacher")
public class Teacher extends BasePO {
	@Column
	private String name;
	@Column
	private String sex;
	@Column
	private Date birthday;
	@OneToMany
	private List<TeacherClass> classes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public List<TeacherClass> getClasses() {
		return classes;
	}

	public void setClasses(List<TeacherClass> classes) {
		this.classes = classes;
	}
}
