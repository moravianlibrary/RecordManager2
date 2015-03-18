package cz.mzk.recordmanager.server.metadata;

import cz.mzk.recordmanager.server.export.ExportFormat;

public interface MetadataRecord {
	
	public String getTitle();
	public String getFormat();
	public Long getPublicationYear();
	public String export(ExportFormat exportFormat);
}
