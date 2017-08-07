package cz.mzk.recordmanager.api.model;

import java.io.File;

public class ImportRecordsDto extends IdDto{

	private File file;

	private String format;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
