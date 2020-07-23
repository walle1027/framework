package org.loed.framework.translate;

import lombok.Data;
import org.loed.framework.common.po.CommonPO;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/4/28 14:48
 */
@Data
@Table(name = "t_job")
public class Job extends CommonPO {
	@Column
	private String jobName;
	@Column
	private String cityName;
	@Column
	private String cityId;
	@ManyToOne
	@JoinColumn(name = "city_id")
	private City city;

	@Override
	public String toString() {
		return "Job{" +
				"jobName='" + jobName + '\'' +
				", cityName='" + cityName + '\'' +
				", city=" + city +
				'}';
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
}
