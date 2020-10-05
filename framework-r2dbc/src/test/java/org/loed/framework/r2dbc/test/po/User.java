package org.loed.framework.r2dbc.test.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/8/2 6:10 PM
 */
@Table(name = "t_user",indexes = {@Index(name = "uk_user",columnList = "account")})
@Data
public class User extends CommonPO {
	@Column
	private String account;
	@Column
	private String username;
	@Column
	private String password;
	@Column
	private String email;
	@Column
	private String mobile;
	@Column
	private LocalDateTime expireAt;
	@Column(columnDefinition = "tinyint default 0")
	private Integer isLocked;
}
