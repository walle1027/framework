package org.loed.framework.r2dbc.po;



import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Table(name = "t_test_person")
@Data
public class Person extends CommonPO {

	@Column(columnDefinition = "VARCHAR(65) NULL COMMENT 'name'")
	private String name;

	@Column(columnDefinition = "datetime COMMENT '生日'")
	private LocalDateTime birthday;

	@Column(columnDefinition = "VARCHAR(10) NULL COMMENT 'sex'")
	private Sex sex;

	@Column(columnDefinition = "int4 NULL COMMENT 'age'")
	private Integer age;
}
