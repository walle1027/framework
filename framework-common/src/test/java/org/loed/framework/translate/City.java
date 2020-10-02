package org.loed.framework.translate;


import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:49
 */
@Table(name = "t_city")
public class City extends CommonPO {
	@Column()
	private String cityName;
	@OneToMany
	private List<Job> jobs;

	@Override
	public String toString() {
		return "City{" +
				"cityName='" + cityName + '\'' +
				", jobs=" + jobs +
				'}';
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
}
