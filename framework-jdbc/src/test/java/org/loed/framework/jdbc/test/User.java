package org.loed.framework.jdbc.test;



import org.loed.framework.common.po.BasePO;

import java.util.Map;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015/2/22 15:35
 */

public class User extends BasePO {
	private String userId;
	private String userName;
	private String name;
	private String naMePaTh;
	private Organize organize;
	private Map<String, Object> orgMap;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNaMePaTh() {
		return naMePaTh;
	}

	public void setNaMePaTh(String naMePaTh) {
		this.naMePaTh = naMePaTh;
	}

	public Organize getOrganize() {
		return organize;
	}

	public void setOrganize(Organize organize) {
		this.organize = organize;
	}

	public Map<String, Object> getOrgMap() {
		return orgMap;
	}

	public void setOrgMap(Map<String, Object> orgMap) {
		this.orgMap = orgMap;
	}
}
