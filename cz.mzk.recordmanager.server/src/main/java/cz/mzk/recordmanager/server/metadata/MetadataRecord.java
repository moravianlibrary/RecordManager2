package cz.mzk.recordmanager.server.metadata;

import java.util.List;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Title;

public interface MetadataRecord {
	
	/**
	 * get all titles of record, first is the most important 
	 * @return List<Title>
	 */
	public List<Title> getTitle();
	
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
	
	public List<Issn> getISSNs();
	
	/**
	 * get all CNBs assigned to record
	 * @return
	 */
	public List<Cnb> getCNBs();
	
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
	public List<HarvestedRecordFormatEnum> getDetectedFormatList();
	
}
