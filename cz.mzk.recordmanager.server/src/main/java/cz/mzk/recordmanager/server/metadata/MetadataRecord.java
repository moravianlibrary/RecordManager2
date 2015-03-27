package cz.mzk.recordmanager.server.metadata;

import cz.mzk.recordmanager.server.export.IOFormat;

public interface MetadataRecord {
	
	public String getTitle();
	public String getFormat();
	public Long getPublicationYear();
	public String export(IOFormat iOFormat);
	
	/**
	 * get unique identifier of record. It must be Institution dependent.
	 * @return
	 */
	public String getUniqueId();
}
