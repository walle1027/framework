package org.loed.framework.r2dbc.test.po;

import org.loed.framework.common.po.*;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/30 9:42 上午
 */
@Table(name = "t_long_id_with_deleted")
@Data
public class LongIdWithDeleted {
	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	protected BigInteger id;
	/**
	 * 数据版本号
	 */
	@Column(nullable = false, updatable = false)
	@Version
	protected BigInteger version;
	/**
	 * 创建时间
	 */
	@Column(updatable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@CreateTime
	protected LocalDateTime createTime;
	/**
	 * 创建者
	 */
	@Column(updatable = false)
	@CreateBy
	protected BigInteger createBy;
	/**
	 * 更新时间
	 */
	@Column(insertable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@LastModifyTime
	protected LocalDateTime updateTime;
	/**
	 * 最后修改者
	 */
	@Column(insertable = false)
	@LastModifyBy
	protected BigInteger updateBy;
	/**
	 * 是否删除
	 */
	@Column(length = 2)
	@IsDeleted
	protected Byte isDeleted;

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

	@Column(length = 20,scale = 2)
	private BigDecimal prop7;

	@Column()
	private LocalDate prop8;

	@Column()
	private LocalDateTime prop9;

	@Column()
	private Boolean prop10;

	@Column()
	private Byte prop11;

	@Column
	private EnumProp prop12;
}
