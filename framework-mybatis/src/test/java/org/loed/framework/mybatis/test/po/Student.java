package org.loed.framework.mybatis.test.po;

import lombok.Data;
import org.loed.framework.common.po.BasePO;

import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/18 10:00 AM
 */
@Data
@Table(name = "t_student", indexes = {@Index(name = "idx_no", columnList = "no")})
public class Student extends BasePO {
	private String no;
	private String name;
	private Integer sex;
	private Date birthday;
}
