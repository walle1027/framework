package org.loed.framework.common.database.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/13 上午11:17
 */
@Table(name = "t_user")
public class User extends BasePO {
	@Column
	private String loginName;
	@Column
	private String mobile;
	@Column
	private String userName;
	@Column
	private String sex;
	@Column
	private Date birthday;
	@Column
	private Integer age;
	@Column
	private Double height;
	@Column
	private Float weight;
	@Column(length = 8, scale = 2)
	private BigDecimal account;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public BigDecimal getAccount() {
		return account;
	}

	public void setAccount(BigDecimal account) {
		this.account = account;
	}
}
