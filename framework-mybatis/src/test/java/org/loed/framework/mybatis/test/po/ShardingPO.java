package org.loed.framework.mybatis.test.po;

import org.loed.framework.common.orm.HashSharding;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/25 4:48 下午
 */
@Table(name = "t_test_sharding_po")
@HashSharding(alias = "sharding", columns = "prop1")
@Data
public class ShardingPO extends BasePO {
	@Column()
	private String prop1;

	@Column()
	private Integer prop2;

	@Column()
	private Double prop3;

	@Column()
	private Float prop4;

	@Column()
	private Long prop5;

	@Column()
	private BigInteger prop6;

	@Column(length = 20, scale = 2)
	private BigDecimal prop7;

	@Column()
	private Date prop8;

	@Column()
	private java.sql.Date prop9;

	@Column()
	private LocalDate prop10;

	@Column()
	private LocalDateTime prop11;

	@Column()
	private Boolean prop12;

	@Column()
	private Byte prop13;
}
