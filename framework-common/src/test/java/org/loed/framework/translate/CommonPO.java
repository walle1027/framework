package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/10 5:12 PM
 */
@Data
public class CommonPO implements Identify {
	/**
	 * 主键
	 */
	@Id
	@Column(nullable = false, updatable = false)
	protected String id;
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
	protected String createBy;
	/**
	 * 更新时间
	 */
	@Column()
	@Temporal(value = TemporalType.TIMESTAMP)
	@LastModifyTime
	protected Date updateTime;
	/**
	 * 最后修改者
	 */
	@Column(length = 32)
	@LastModifyBy
	protected String updateBy;
	/**
	 * 是否删除
	 */
	@Column(length = 2)
	@IsDeleted
	protected Integer isDeleted;
}
