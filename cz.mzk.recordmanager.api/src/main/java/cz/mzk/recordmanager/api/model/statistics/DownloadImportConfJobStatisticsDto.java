package cz.mzk.recordmanager.api.model.statistics;

public class DownloadImportConfJobStatisticsDto extends GeneralStatisticsDto {

	private Long importConfId;

	private String libraryName;

	private String url;

	private String importJobName;

	private String format;

	private Long noOfRecords;

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

	public String getImportJobName() {
		return importJobName;
	}

	public void setImportJobName(String importJobName) {
		this.importJobName = importJobName;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Long getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(Long noOfRecords) {
		this.noOfRecords = noOfRecords;
	}

	public Long getImportConfId() {
		return importConfId;
	}

	public void setImportConfId(Long importConfId) {
		this.importConfId = importConfId;
	}

}
