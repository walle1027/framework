package org.loed.framework.translate;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:47
 */
@Table(name = "t_son")
@Data
public class Son extends CommonPO {
	@Column
	private String name;
	@Column
	private String sex;
	@Column
	private int age;
	@Column
	private String jobId;
	@ManyToOne
	@JoinColumn(name = "job_id")
	private Job job;
	@OneToMany
	private List<People> friends;

	@Override
	public String toString() {
		return "Son{" +
				"name='" + name + '\'' +
				", sex='" + sex + '\'' +
				", age=" + age +
				", job=" + job +
				'}';
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<People> getFriends() {
		return friends;
	}

	public void setFriends(List<People> friends) {
		this.friends = friends;
	}
}
