package org.loed.framework.r2dbc.po;

import lombok.Data;
import org.loed.framework.common.po.CreateTime;
import org.loed.framework.common.po.LastModifyTime;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/5 1:01 下午
 */
@Data
@Table(name = "t_people")
public class People {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	private Long id;
	@Column
	@Version
	private Long version;
	@Column
	private String name;
	@Column
	private Byte sex;
	@Column
	private String race;
	@Column
	@CreateTime
	private LocalDateTime createTime;
	@Column
	@LastModifyTime
	private LocalDateTime updateTime;
}
