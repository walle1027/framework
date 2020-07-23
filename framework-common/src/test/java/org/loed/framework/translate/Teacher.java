package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.*;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:39
 */
@Data
@Table(name = "t_teacher")
public class Teacher extends CommonPO {
	@Column
	private String name;
	@OneToMany
	private List<Student> students;
	@Column
	private String stuffId;
	@ManyToOne
	@JoinColumn(name = "stuff_id")
	private Stuff stuff;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}
}
