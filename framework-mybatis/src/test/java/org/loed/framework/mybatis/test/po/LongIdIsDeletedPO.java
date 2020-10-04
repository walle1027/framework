package org.loed.framework.mybatis.test.po;

import org.loed.framework.common.po.*;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/29 4:58 下午
 */
@Table(name = "t_test_long_id_is_deleted_po")
@Data
public class LongIdIsDeletedPO {
	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	protected Long id;
	/**
	 * 数据版本号
	 */
	@Column(nullable = false, updatable = false)
	@Version
	protected Long version;
	/**
	 * 创建时间
	 */
	@Column(updatable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@CreateTime
	protected Date createTime;
	/**
	 * 创建者
	 */
	@Column(length = 32, updatable = false)
	@CreateBy
	protected Long createBy;
	/**
	 * 更新时间
	 */
	@Column(insertable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@LastModifyTime
	protected Date updateTime;
	/**
	 * 最后修改者
	 */
	@Column(length = 32, insertable = false)
	@LastModifyBy
	protected Long updateBy;

	@Column()
	@IsDeleted
	private Byte isDeleted;

	@Column()
	private String prop1;

	@Column()
	private Integer prop2;

	@Column()
	private Double prop3;

	@Column()
	private Float prop4;

	@Column()
	private long prop5;

	@Column()
	private BigInteger prop6;

	@Column(length = 20,scale = 2)
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
