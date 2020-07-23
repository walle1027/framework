package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:40
 */
@Data
@Table(name = "t_class_room")
public class ClassRoom extends CommonPO {
	@Column
	private String no;
	@Column
	private String gradeId;
	@ManyToOne
	@JoinColumn(name = "grade_id")
	private Grade grade;

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}
}
