package org.loed.framework.r2dbc.po;


import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/7/12 下午2:36
 */
@Table(name = "t_test_student")
public class Student extends CommonPO {
	@Column
	private String no;
	@Column
	private String name;
	@Column
	private String classId;
	@ManyToOne
	@JoinColumn(name = "class_id")
	private Class myClass;

	@Column
	private String classId2;
	@ManyToOne
	@JoinColumn(name = "class_id2")
	private Class myClass2;

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public Class getMyClass() {
		return myClass;
	}

	public void setMyClass(Class myClass) {
		this.myClass = myClass;
	}

	public String getClassId2() {
		return classId2;
	}

	public void setClassId2(String classId2) {
		this.classId2 = classId2;
	}

	public Class getMyClass2() {
		return myClass2;
	}

	public void setMyClass2(Class myClass2) {
		this.myClass2 = myClass2;
	}
}
