package org.loed.framework.rpc.test.service;

import org.loed.framework.common.Result;
import org.loed.framework.common.query.PageRequest;
import org.loed.framework.rpc.ServiceProxy;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ServiceProxy(baseUri = "http://localhost:3000")
public interface StudentService {
	//----------------get mapping--------------//
	@GetMapping(value = "/student/{id}")
	Student getStudent(@PathVariable("id") Long id);

	@GetMapping(value = "/student/{id}")
	Mono<Student> getStudent2(@PathVariable("id") Long id);

	@GetMapping(value = "/student/{id}")
	Flux<Student> getStudent3(@PathVariable("id") Long id);

	@GetMapping(value = "/student/wrapper/{id}")
	Mono<Result<Student>> getStudent5(@PathVariable("id") Long id);

	@GetMapping(value = "/student/{id}")
	CompletableFuture<Student> getStudent4(@PathVariable("id") Long id);


	//----------------post mapping--------------//
	@PostMapping("/student")
	Long addStudent(@RequestBody Student student);

	@PostMapping("/student")
	Flux<Long> addStudent2(@RequestBody Student student);

	@PostMapping("/student")
	Mono<Long> addStudent3(@RequestBody Student student);

	@PostMapping("/student")
	CompletableFuture<Long> addStudent4(@RequestBody Student student);

	//----------------put mapping--------------//
	@PutMapping("/student")
	Student putStudent(@RequestBody Student student);

	@PutMapping("/student")
	Flux<Student> putStudent2(@RequestBody Student student);

	@PostMapping("/student")
	Mono<Student> putStudent3(@RequestBody Student student);

	@PostMapping("/student")
	CompletableFuture<Student> putStudent4(@RequestBody Student student);


	//----------------delete mapping--------------//
	@DeleteMapping("/student/{id}")
	void deleteStudent(@PathVariable("id") Long id);

	@DeleteMapping("/student/{id}")
	Flux<Void> deleteStudent2(@PathVariable("id") Long id);

	@DeleteMapping("/student/{id}")
	Mono<Void> deleteStudent3(@PathVariable("id") Long id);

	@DeleteMapping("/student/{id}")
	CompletableFuture<Void> deleteStudent4(@PathVariable("id") Long id);


	//POST with param
	@PostMapping("/student/query")
	List<Student> getStudentList(@RequestParam("id") List<Long> idList, @RequestBody PageRequest pageRequest);

	@PostMapping("/student/query")
	List<Student> getStudentList2(@RequestParam("id") long[] idList, @RequestBody PageRequest pageRequest);

	//POST with file
	@PostMapping("/student/profile/upload/{id}")
	Student uploadStudentProfile(@PathVariable("id") Long id, @RequestParam("name") String name, @RequestPart() File profile);
}
