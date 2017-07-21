package cz.mzk.recordmanager.api.model.statistics;

import java.util.Date;

public class OaiHarvestJobStatisticsDto extends GeneralStatisticsDto {

	private Long importConfId;

	private String libraryName;

	private String url;

	private Date fromParam;

	private Date toParam;

	private Long noOfRecords;

	private Long libraryId;

	public Long getImportConfId() {
		return importConfId;
	}

	public void setImportConfId(Long importConfId) {
		this.importConfId = importConfId;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	public Date getFromParam() {
		return fromParam;
	}

	public void setFromParam(Date fromParam) {
		this.fromParam = fromParam;
	}

	public Date getToParam() {
		return toParam;
	}

	public void setToParam(Date toParam) {
		this.toParam = toParam;
	}

	public Long getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(Long noOfRecords) {
		this.noOfRecords = noOfRecords;
	}

	public Long getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(Long libraryId) {
		this.libraryId = libraryId;
	}

}
