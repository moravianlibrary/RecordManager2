package cz.mzk.recordmanager.server.metadata;

import java.util.Collections;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;

import java.util.List;
import java.util.Set;

public interface MetadataRecord {

	/**
	 * get all titles of record, first is the most important
	 *
	 * @return List<Title>
	 */
	List<Title> getTitle();

	/**
	 * get year of publication
	 *
	 * @return Long
	 */
	Long getPublicationYear();

	/**
	 * export record in given format
	 *
	 * @param iOFormat {@link IOFormat}
	 * @return string representation of record
	 */
	String export(IOFormat iOFormat);

	/**
	 * get unique identifier of record. It must be Institution dependent.
	 *
	 * @return unique id
	 */
	String getUniqueId();

	/**
	 * get all {@link Isbn} assigned to record
	 *
	 * @return List<Isbn>
	 */

	List<Isbn> getISBNs();

	/**
	 * get all {@link Issn} assigned to record
	 *
	 * @return List<Issn>
	 */

	List<Issn> getISSNs();

	/**
	 * get all {@link Cnb} assigned to record
	 *
	 * @return List<Cnb>
	 */
	List<Cnb> getCNBs();

	/**
	 * get series ISSN
	 *
	 * @return String or null
	 */
	String getISSNSeries();

	/**
	 * get ISSN series order
	 *
	 * @return String or null
	 */
	String getISSNSeriesOrder();

	/**
	 * get count of pages
	 *
	 * @return page count or null
	 */
	Long getPageCount();

	/**
	 * get weight of record
	 *
	 * @param baseWeight from db
	 * @return Long
	 */
	Long getWeight(Long baseWeight);

	/**
	 * return list of detected formats {@link HarvestedRecordFormatEnum}
	 *
	 * @return List<HarvestedRecordFormatEnum>
	 */
	List<HarvestedRecordFormatEnum> getDetectedFormatList();

	/**
	 * Decide if this record has {@link HarvestedRecordFormatEnum#BLIND_BRAILLE}
	 *
	 * @return true or false
	 */
	default boolean isBlindBraille() {
		return false;
	}

	/**
	 * Decide if this record has {@link HarvestedRecordFormatEnum#MUSICAL_SCORES}
	 *
	 * @return true or false
	 */
	default boolean isMusicalScores() {
		return false;
	}

	/**
	 * Decide if this record has {@link HarvestedRecordFormatEnum#VIDEO_DOCUMENTS}
	 *
	 * @return true or false
	 */
	default boolean isVisualDocument() {
		return false;
	}

	/**
	 * return scale of document (significant for maps only)
	 *
	 * @return Long or null
	 */
	Long getScale();

	/**
	 * return UUID of ducument
	 *
	 * @return String or null
	 */
	String getUUId();

	/**
	 * get authority key for main author
	 *
	 * @return String or null
	 */
	String getAuthorAuthKey();

	/**
	 * get authority keys for all authors
	 *
	 * @return String or null
	 */
	default List<Authority> getAllAuthorAuthKey() {
		return null;
	}

	/**
	 * get string representing main author
	 *
	 * @return String or null
	 */
	String getAuthorString();

	/**
	 * get cluster id of record
	 *
	 * @return String or null
	 */
	String getClusterId();

	/**
	 * get list of {@link Oclc}
	 *
	 * @return List<Oclc>
	 */
	List<Oclc> getOclcs();

	/**
	 * get list o {@link Language}
	 *
	 * @return List<String>
	 */
	List<String> getLanguages();

	/**
	 * Decide whether this record should be stored during importing/harvesting
	 *
	 * @return true if record should be stored, false otherwise
	 */
	boolean matchFilter();

	/**
	 * return raw identifier from field 001
	 *
	 * @return String
	 */
	String getRaw001Id();

	/**
	 * return record format for citation
	 *
	 * @return {@link CitationRecordType}
	 */
	CitationRecordType getCitationFormat();

	/**
	 * return barcodes
	 *
	 * @return List<String>
	 */
	default List<String> getBarcodes() {
		return null;
	}

	/**
	 * get all ISMNs assigned to record
	 *
	 * @return List<Ismn>
	 */
	List<Ismn> getISMNs();

	/**
	 * get authority ID of main author
	 * only for authority records
	 * @return String
	 */
	String getAuthorityId();

	/**
	 * get urls
	 * format: availability | link | comment
	 *
	 * @return List<String>
	 */
	List<String> getUrls();

	/**
	 * get rights value from Kramerius
	 *
	 * @return String
	 */
	String getPolicyKramerius();

	/**
	 * get model from Kramerius
	 *
	 * @return String
	 */
	default String getModelKramerius() {
		return "unknown";
	}

	/**
	 * get stopwords file name
	 *
	 * @return String
	 */
	String filterSubjectFacet();

	/**
	 * get all {@link Ean} assigned to record
	 *
	 * @return List<Ean>
	 */
	List<Ean> getEANs();

	/**
	 * get all {@link ShortTitle} assigned to record
	 *
	 * @return List<ShortTitle>
	 */
	List<ShortTitle> getShortTitles();

	/**
	 * get statuses with institution dependence
	 *
	 * @return List<String>
	 */
	List<String> getDefaultStatuses();

	/**
	 * get international patent classfication for UPV
	 *
	 * @return List<String>
	 */
	List<String> getInternationalPatentClassfication();

	/**
	 * get all {@link TezaurusKey} assigned to record
	 *
	 * @return List<TezaurusKey>
	 */
	TezaurusKey getTezaurusKey();

	/**
	 * get metaproxy permission
	 *
	 * @return Boolean
	 */
	Boolean getMetaproxyBool();

	default boolean getIndexWhenMerged() {
		return true;
	}

	/**
	 * get all {@link PublisherNumber} assigned to record
	 *
	 * @return List<PublisherNumber>
	 */
	List<PublisherNumber> getPublisherNumber();

	/**
	 * get sfx url
	 *
	 * @param id record id
	 * @return String
	 */
	default String getSfxUrl(String id) {
		return null;
	}

	/**
	 * get application id, record id end with _B6
	 *
	 * @return String or null
	 */
	default String getUpvApplicationId() {
		return null;
	}

	/**
	 * get source info field 773, subfield x
	 *
	 * @return String or null
	 */
	String getSourceInfoX();

	/**
	 * get source info field 773, subfield t
	 *
	 * @return String or null
	 */
	String getSourceInfoT();

	/**
	 * get source info field 773, subfield g
	 *
	 * @return String or null
	 */
	String getSourceInfoG();

	/**
	 * get siglas from caslin records
	 * @return {@link Set} of String
	 */
	default Set<String> getCaslinSiglas() {
		return Collections.emptySet();
	}

	/**
	 * get sigla from field SGLa, only for libraries
	 *
	 * @return sigla as String
	 */
	default String getLibrarySigla() {
		return null;
	}

	/**
	 * get id and name of reginal library, only for libraries
	 *
	 * @return regional library id | name
	 */
	default String getRegionalLibrary() {
		return null;
	}
}
