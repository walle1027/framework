package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/7/22 5:37 下午
 */
@Data
@Table(name = "t_stuff")
public class Stuff extends CommonPO {
	@Column
	private String name;
	@Column
	private Integer age;
}
