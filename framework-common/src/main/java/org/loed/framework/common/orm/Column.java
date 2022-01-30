package org.loed.framework.common.orm;

import lombok.Data;
import lombok.ToString;

import javax.persistence.GenerationType;
import java.sql.SQLType;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/10/9 上午9:23
 */
@Data
@ToString(exclude = "table")
public class Column {
	/**
	 * 关联的表
	 */
	private Table table;
	/**
	 * The java.sql.Types type
	 */
	private SQLType sqlType;
	/**
	 * 数据库列名称
	 */
	private String sqlName;
	/**
	 * 长度
	 */
	private int length;
	/**
	 * 精度
	 */
	private int precision;
	/**
	 * 刻度
	 */
	private int scale;
	/**
	 * 是否为空
	 */
	private boolean nullable;
	/**
	 * 是否是索引
	 */
	private boolean indexed;
	/**
	 * 是否是唯一索引
	 */
	private boolean unique;
	/**
	 * 是否版本列
	 */
	private boolean versioned;
	/**
	 * 是否可以新增
	 */
	private boolean insertable = true;
	/**
	 * 是否可以修改
	 */
	private boolean updatable = true;
	/**
	 * 是否是租户Id
	 */
	private boolean tenantId;
	/**
	 * 是否是创建人
	 */
	private boolean createBy;
	/**
	 * 是否是创建时间
	 */
	private boolean createTime;
	/**
	 * 是否是最后修改人
	 */
	private boolean lastModifyBy;
	/**
	 * 是否是最后修改时间
	 */
	private boolean lastModifyTime;
	/**
	 * 是否是删除标记
	 */
	private boolean deleted;
	/**
	 * 默认值
	 */
	private String defaultValue;
	/**
	 * 是否是主键
	 */
	private boolean isPk;
	/**
	 * 主键生成方式
	 */
	private GenerationType idGenerationType;
	/**
	 * 列注释
	 */
	private String sqlComment;
	/**
	 * java类型
	 */
	private Class<?> javaType;
	/**
	 * java名称
	 */
	private String javaName;
	/**
	 * columnDefinition
	 */
	private String columnDefinition;
	/**
	 * 是否分表列
	 */
	private boolean shardingColumn;

	public Column(Table table) {
		this.table = table;
	}
}
