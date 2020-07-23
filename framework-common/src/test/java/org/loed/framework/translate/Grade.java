package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:40
 */
@Data
@Table(name = "t_grade")
public class Grade extends CommonPO {
	@Column
	private String name;

	private List<ClassRoom> classRooms;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ClassRoom> getClassRooms() {
		return classRooms;
	}

	public void setClassRooms(List<ClassRoom> classRooms) {
		this.classRooms = classRooms;
	}
}
