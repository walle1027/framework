package org.loed.framework.r2dbc.dao;

import org.loed.framework.r2dbc.po.Person;
import org.loed.framework.r2dbc.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/4/30 7:31 PM
 */
public interface PersonDao extends R2dbcDao<Person, String> {
	@Query("select * from t_test_person where name = :name")
	Flux<Person> all(String name);

	@Query("select count(*) from t_test_person where name = :name")
	Mono<Long> count(String name);
}
