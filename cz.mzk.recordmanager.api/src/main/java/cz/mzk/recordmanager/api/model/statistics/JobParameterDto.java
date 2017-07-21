package cz.mzk.recordmanager.api.model.statistics;

import java.util.Date;

public class JobParameterDto {

	private String typeCd;

	private String keyName;

	private String stringVal;

	private Date dateVal;

	private Long longVal;

	private Double doubleVal;

	private String identifying;

	public String getTypeCd() {
		return typeCd;
	}

	public void setTypeCd(String typeCd) {
		this.typeCd = typeCd;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getStringVal() {
		return stringVal;
	}

	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

	public Date getDateVal() {
		return dateVal;
	}

	public void setDateVal(Date dateVal) {
		this.dateVal = dateVal;
	}

	public Long getLongVal() {
		return longVal;
	}

	public void setLongVal(Long longVal) {
		this.longVal = longVal;
	}

	public Double getDoubleVal() {
		return doubleVal;
	}

	public void setDoubleVal(Double doubleVal) {
		this.doubleVal = doubleVal;
	}

	public String getIdentifying() {
		return identifying;
	}

	public void setIdentifying(String identifying) {
		this.identifying = identifying;
	}

}
