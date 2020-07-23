package org.loed.framework.rpc.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loed.framework.common.Result;
import org.loed.framework.common.query.PageRequest;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.rpc.test.service.Student;
import org.loed.framework.rpc.test.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RpcTest.class)
public class ServiceProxyTest {
	@Autowired
	private StudentService studentService;

	@Test
	public void testGet() {
		Long id = 1L;
		Student student = studentService.getStudent(id);
		Assert.assertEquals(student.getName(), "张三");
	}

	@Test
	public void testGetMono() {
		Mono<Student> mono = studentService.getStudent2(1L);
		Student student = mono.block();
		Assert.assertEquals(student.getName(), "张三");
	}

	@Test
	public void testWrapper() {
		Mono<Result<Student>> mono = studentService.getStudent5(1L);
		Result<Student> result = mono.block();
		Assert.assertEquals(result.getCode(), 0);
		Assert.assertEquals(result.getData().getName(), "张三");
	}

	@Test
	public void testPost() {
		Student student = new Student();
		student.setName("张三");
		student.setGender("男");
		student.setGrade("1");
		student.setHeight(1.45F);
		student.setWeight(39.2f);
		student.setNum(1L);
		student.setRace("汉");
		long id = studentService.addStudent(student);
		Assert.assertEquals(id, 1);
	}


	@Test
	public void testPut() {
		Student student = new Student();
		student.setName("张三");
		student.setGender("男");
		student.setGrade("1");
		student.setHeight(1.45F);
		student.setWeight(39.2f);
		student.setNum(1L);
		student.setRace("汉");
		Student student1 = studentService.putStudent(student);
		System.out.println(SerializeUtils.toJson(student1));
	}


	@Test
	public void testDelete() {
		studentService.deleteStudent(1L);
	}


	@Test
	public void testPostWithParams() {
		List<Long> idList = new ArrayList<>();
		idList.add(1L);
		idList.add(2L);
		idList.add(3L);
		PageRequest request = new PageRequest();
		request.setPageNo(1);
		request.setPageSize(100);
		request.setNeedCount(true);
		request.setNeedPaging(true);
		List<Student> studentList = studentService.getStudentList(idList, request);
		for (Student student : studentList) {
			System.out.println(SerializeUtils.toJson(student));
		}
	}

	@Test
	public void testPostWithParams2() {
		long[] idList = new long[4];
		idList[0] = 1L;
		idList[1] = 2L;
		idList[2] = 3L;
		idList[3] = 4L;
		PageRequest request = new PageRequest();
		request.setPageNo(1);
		request.setPageSize(100);
		request.setNeedCount(true);
		request.setNeedPaging(true);
		List<Student> studentList = studentService.getStudentList2(idList, request);
		for (Student student : studentList) {
			System.out.println(SerializeUtils.toJson(student));
		}
	}


	@Test
	public void testUploadProfile() {
		Student student = studentService.uploadStudentProfile(1L, "张三", new File("D:\\work\\framework\\framework-rpc\\pom.xml"));
		System.out.println(student);
	}
}
