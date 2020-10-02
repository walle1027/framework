package org.loed.framework.translate;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 15:39
 */
@Data
@Table(name = "t_people")
public class People extends CommonPO {
	@Column
	private String name;
	@Column
	private int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
