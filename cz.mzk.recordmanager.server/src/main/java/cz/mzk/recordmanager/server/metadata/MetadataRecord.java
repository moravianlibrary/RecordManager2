package cz.mzk.recordmanager.server.metadata;

import java.util.List;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Ismn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Language;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.PublisherNumber;
import cz.mzk.recordmanager.server.model.ShortTitle;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;
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
	
	/**
	 * get list of {@link Oclc}
	 * @return
	 */
	public List<Oclc> getOclcs();
	
	/**
	 * get list o {@link Language}
	 * @return
	 */
	public List<String> getLanguages();
	
	/**
	 * Decide whether this record should be stored during importing/harvesting
	 * @return true if record should be stored, false otherwise
	 */
	public boolean matchFilter();
	
	/**
	 * get record id
	 * @return
	 */
	public String getOAIRecordId();
	
	/**
	 * return raw identifier from field 001
	 * @return
	 */
	public String getRaw001Id();
	
	
	/**
	 * return true if record is deleted from field DEL
	 * @return
	 */
	public Boolean isDeleted();
	
	/**
	 * return record format for citation
	 * @return
	 */
	public CitationRecordType getCitationFormat();
	
	/**
	 * return barcodes
	 * @return
	 */
	default public List<String> getBarcodes(){
		return null;
	}
	
	/**
	 * get all ISMNs assigned to record
	 * @return List<Ismn>
	 */
	public List<Ismn> getISMNs();
	
	/**
	 * get authority ID
	 * @return
	 */
	public String getAuthorityId();

	/**
	 * get urls
	 * format: availability | link | comment
	 * @return
	 */
	public List<String> getUrls();

	/**
	 * get rights value from Kramerius
	 * @return
	 */
	public String getPolicyKramerius();
	
	/**
	 * get stopwords file name
	 * @return
	 */
	public String filterSubjectFacet();
	
	public List<Ean> getEANs();

	public List<ShortTitle> getShortTitles();
	
	public List<String> getDefaultStatuses();

	public List<String> getInternationalPatentClassfication();

	public TezaurusKey getTezaurusKey();

	public Boolean getMetaproxyBool();

	default public boolean getIndexWhenMerged() {
		return true;
	}

	public List<PublisherNumber> getPublisherNumber();

	default public String getSfxUrl(String id) {
		return null;
	}

	public String getSourceInfoX();

	public String getSourceInfoT();

	public String getSourceInfoG();
}
