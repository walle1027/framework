package org.loed.framework.common.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.criteria.JoinType;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/4 11:51 上午
 */
@Data
@EqualsAndHashCode
@ToString
public class Join {
	/**
	 * 表之间的关联方式{@link JoinType#INNER,JoinType#LEFT,JoinType#RIGHT}
	 */
	private JoinType joinType = JoinType.INNER;
	/**
	 * 关联的目标的表
	 */
	private String target;
	/**
	 * 唯一的关联路径
	 */
	private String uniquePath;
}
