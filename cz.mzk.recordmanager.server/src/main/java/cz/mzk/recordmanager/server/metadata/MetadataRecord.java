package cz.mzk.recordmanager.server.metadata;

import java.util.List;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.Isbn;

public interface MetadataRecord {
	
	/**
	 * get all titles of record, first is the most important 
	 * @return List<String>
	 */
	public List<String> getTitle();
	
	public String getFormat();
	public Long getPublicationYear();
	
	/**
	 * export record in given format
	 * @param iOFormat
	 * @return
	 */
	public String export(IOFormat iOFormat);
	
	/**
	 * get unique identifier of record. It must be Institution dependent.
	 * @return
	 */
	public String getUniqueId();
	
	/**
	 * get all ISBNs assigned to record
	 * @return List<String>
	 */
	
	public List<Isbn> getISBNs();
	
	/**
	 * get all ISSNs assigned to record
	 * @return List<String>
	 */
	
	public List<String> getISSNs();
	
	/**
	 * get series ISSN
	 * @return String or null
	 */
	public String getSeriesISSN();
	
	/**
	 * get count of pages
	 * @return page count or null
	 */
	public Long getPageCount();
	
	/**
	 * return list of detected formats
	 * @return List<RecordFormat>
	 */
	public List<HarvestedRecordFormat> getDetectedFormatList();
	
}
