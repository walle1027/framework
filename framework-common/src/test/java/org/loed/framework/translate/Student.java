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
@Table(name = "t_student")
@Data
public class Student extends CommonPO {
	@Column
	private int age;
	@Column
	private String name;
	@Column
	private String code;
	@Column
	private String teacherId;
	@ManyToOne
	@JoinColumn(name = "teacher_id")
	private Teacher teacher;
	@Column
	private String classRoomId;
	@Column
	@JoinColumn(name = "class_room_id")
	private ClassRoom classRoom;
	@OneToMany
	private List<People> friends;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public ClassRoom getClassRoom() {
		return classRoom;
	}

	public void setClassRoom(ClassRoom classRoom) {
		this.classRoom = classRoom;
	}

	public List<People> getFriends() {
		return friends;
	}

	public void setFriends(List<People> friends) {
		this.friends = friends;
	}
}
