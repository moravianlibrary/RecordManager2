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
	public String getISSNSeries();
	
	/**
	 * get ISSN series order
	 * @return
	 */
	public String getISSNSeriesOrder();
	
	/**
	 * get count of pages
	 * @return page count or null
	 */
	public Long getPageCount();
	
	/**
	 * get weight of record
	 * @param baseWeight
	 * @return Long
	 */
	public Long getWeight(Long baseWeight);
	
	/**
	 * return list of detected formats
	 * @return List<RecordFormat>
	 */
	public List<HarvestedRecordFormatEnum> getDetectedFormatList();
	
	/**
	 * return scale of document (significant for maps only)
	 * @return Long or null
	 */
	public Long getScale();
	
	/**
	 * return UUID of ducument
	 * @return
	 */
	public String getUUId();
	
	/**
	 * get authority key for main author
	 * @return
	 */
	public String getAuthorAuthKey();
	
	/**
	 * get string representing main author
	 * @return
	 */
	public String getAuthorString();
	
	/**
	 * get cluster id of record
	 * @return
	 */
	public String getClusterId();
}
