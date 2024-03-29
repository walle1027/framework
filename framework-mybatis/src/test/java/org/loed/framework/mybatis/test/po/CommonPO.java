package org.loed.framework.mybatis.test.po;

import org.loed.framework.common.po.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/10 5:12 PM
 */
@Data
public class CommonPO {
	/**
	 * 主键
	 */
	@Id
	@Column(nullable = false, updatable = false, length = 32)
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
	@Column(insertable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@LastModifyTime
	protected Date updateTime;
	/**
	 * 最后修改者
	 */
	@Column(length = 32, insertable = false)
	@LastModifyBy
	protected String updateBy;
	/**
	 * 是否删除
	 */
	@Column(length = 2)
	@IsDeleted
	protected Byte isDeleted;
}
