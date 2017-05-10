package cz.mzk.recordmanager.server.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.ISBNValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Ismn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.ShortTitle;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class MetadataDublinCoreRecord implements MetadataRecord {

	private static Logger logger = LoggerFactory
			.getLogger(MetadataDublinCoreRecord.class);

	protected DublinCoreRecord dcRecord;

	protected final ISBNValidator isbnValidator = ISBNValidator
			.getInstance(true);

	protected static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	protected static final Pattern ISSN_PATTERN = Pattern.compile("(\\d{4}-\\d{3}[\\dxX])(.*)");
	protected static final Pattern ISBN_PATTERN = Pattern.compile("([\\dxX\\s\\-]*)(.*)");
	
	protected static final Pattern DC_UUID_PATTERN = Pattern.compile("^uuid:(.*)",Pattern.CASE_INSENSITIVE);
/*	protected static final Pattern DC_ISBN_PATTERN = Pattern
			.compile("isbn:(.*),Pattern.CASE_INSENSITIVE");
*/
	protected static final Pattern DC_ISBN_PATTERN = Pattern
			.compile("isbn:\\s*([\\dxX-]*)",Pattern.CASE_INSENSITIVE);
	
	protected static final Pattern DC_ISSN_PATTERN = Pattern
			.compile("issn:(.*)",Pattern.CASE_INSENSITIVE);
	protected static final Pattern DC_CNB_PATTERN = Pattern
			.compile("^ccnb:(.*)",Pattern.CASE_INSENSITIVE);
	protected static final Pattern DC_OCLC_PATTERN = Pattern
			.compile("^oclc:(.*)",Pattern.CASE_INSENSITIVE);
	protected static final Pattern DC_ISMN_PATTERN = Pattern
			.compile("^ismn:([\\dM\\s\\-]*)(.*)",Pattern.CASE_INSENSITIVE);
	protected static final Pattern DC_IDENTIFIER_PATTERN = Pattern
			.compile(".*:.*",Pattern.CASE_INSENSITIVE);
	
	protected static final Pattern DC_TYPE_KRAMERIUS_PATTERN = Pattern.compile("^model:(.*)");

	protected static final String ISMN_CLEAR_REGEX = "[^0-9^M]";
	protected static final String ISMN10_PREFIX = "M";
	protected static final String ISMN13_PREFIX = "9790";
	
	public MetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		if (dcRecord == null) {
			throw new IllegalArgumentException(
					"Creating MetadataDublinCoreRecord with NULL underlying dcRecord.");
		}
		this.dcRecord = dcRecord;
	}

	@Override
	public List<Title> getTitle() {
		List<Title> result = new ArrayList<Title>();
		List<String> dcTitles = dcRecord.getTitles();
		Long titleOrder = 0L;
		
		for (String s: dcTitles) {
			Title title = new Title();
			title.setTitleStr(s);
			title.setSimilarityEnabled(MetadataUtils.similarityEnabled(title));
			title.setOrderInRecord(++titleOrder);
			result.add(title);
		}
		
		/*returns "" if no title is found - same as MARC implementation*/
		if (result.isEmpty()) {
			Title title = new Title();
			title.setTitleStr("");
			title.setOrderInRecord(1L);
			result.add(title);
		}
		
		return result;
	}

	/* <MJ.> -- method removed from supertype
	@Override
	public String getFormat() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			//Kramerius specific
			if (f.equals("model:monograph")) {
				return "Book";
			//Kramerius specific
			} else if (f.equals("model:periodical")) {
				return "Journal";
			}
		}
		return null;
	}
*/

	@Override
	public Long getPublicationYear() {
		// expecting year in date (should work for Kramerius)
		String year = dcRecord.getFirstDate();
		if (year == null) {
			return null;
		}
		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {
		}
		return null;
	}

	@Override
	public String export(IOFormat iOFormat) {
		if (iOFormat.equals(IOFormat.DC_XML)) {
			return dcRecord.getRawRecord() == null ? "" : dcRecord.getRawRecord().toString();
		}
		return "";
	}

	@Override
	public String getUniqueId() {
		// expecting unique id in first identifier
		return this.dcRecord.getFirstIdentifier();
	}

	@Override
	public List<Isbn> getISBNs() {
		/*
		 * go through all identifiers, look for isbn:.*, validate isbn, return isbn
		 * list
		 */
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Isbn> isbns = new ArrayList<Isbn>();		
		Pattern p = DC_ISBN_PATTERN;
		Matcher m;
		Long isbnCounter = 0L;

		for (String f : identifiers) {

			m = p.matcher(f);
			String isbnStr;
			if(m.find()){
				isbnStr = m.group(1).trim();
			}
			else{
				if(DC_IDENTIFIER_PATTERN.matcher(f).matches()) continue;
				isbnStr = f.trim();
			}

			if(ISBN_PATTERN.matcher(isbnStr).find()){
				try {
					String isbnValidStr = isbnValidator.validate(isbnStr);
					Isbn isbn = new Isbn();
					isbn.setIsbn(Long.valueOf(isbnValidStr));

					isbn.setNote("");
					isbn.setOrderInRecord(++isbnCounter);

					isbns.add(isbn);
				} catch (NumberFormatException nfe) {
					logger.info(String.format("Invalid ISBN: %s", isbnStr));
					continue;
				}
			}
		}
		return isbns;
	}

	@Override
	public List<Issn> getISSNs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Issn> issns = new ArrayList<Issn>();
		Long issnCounter = 0L;
		
		Pattern p = DC_ISSN_PATTERN;
		Matcher m;

		for (String id : identifiers) {
			
			m = p.matcher(id);
			String dcIssn;
			if(m.find()) dcIssn = m.group(1);
			else{
				if(DC_IDENTIFIER_PATTERN.matcher(id).matches()) continue;
				dcIssn = id;
			}

			Matcher matcher = ISSN_PATTERN.matcher(dcIssn);
			
			try {
				if(matcher.find()) {
					Issn issn = new Issn();
					if(!issn.issnValidator(matcher.group(1).trim())){
						throw new NumberFormatException();
					}					
					issn.setIssn(matcher.group(1).trim());						
					issn.setNote("");
					issn.setOrderInRecord(++issnCounter);
					issns.add(issn);
				}			
			} catch (NumberFormatException e) {
				logger.info(String.format("Invalid ISSN: %s", dcIssn));
				continue;
			}
		}
		return issns;
	}

	@Override
	public Long getPageCount() {
		// getting page count from dc:format via regexp is not accurate enough ... leaving with null
		return null;
	}

	// note: recognized Kramerius formats are in c.m.r.server.kramerius.FedoraModels;
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<HarvestedRecordFormatEnum>();

		if(isBook()) hrf.add(HarvestedRecordFormatEnum.BOOKS);
		if(isPeriodical()) hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		if(isMap()) hrf.add(HarvestedRecordFormatEnum.MAPS);
		if(isVisual()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if(isMusicalScore()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if(isAudioDocument()) hrf.add(HarvestedRecordFormatEnum.AUDIO_OTHER);
		if(isOtherDocument()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		return hrf;
	}
	
	
	protected boolean isBook() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:monograph") || f.equals("Digitized Monograph")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isPeriodical() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:periodical") || f.equals("Digitized Periodical")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isMap() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:map")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isVisual() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:graphic")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isMusicalScore() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:sheetmusic")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isAudioDocument() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:soundrecording")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isOtherDocument() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:archive") || f.equals("model:manuscript") ) {
				return true;
			}
		}
		return false;
	}
	
	/* there may be more than one rights value in Kramerius, but only one "policy:.*" */
	public String getPolicyKramerius(){
		List<String> rights = dcRecord.getRights();
		String policy = "unknown";
		for (String f : rights) {
			if (f.equals("policy:public")) {
				return "public";
			} else if (f.equals("policy:private")) {
				return "private";
			}
		}
		return policy;
	}
	
	public String getModelKramerius(){
		List<String> types = dcRecord.getTypes();
		Pattern p = DC_TYPE_KRAMERIUS_PATTERN;
		Matcher m;
		String model = "unknown";

		for (String f : types) {
			m = p.matcher(f);

			if (m.find()) {
				return m.group(1).trim();
			}
		}
		
		return model;
	}
	


	@Override
	public List<Cnb> getCNBs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Cnb> cnbs = new ArrayList<Cnb>();
		
		Pattern p = DC_CNB_PATTERN;
		Matcher m;

		for (String f : identifiers) {
			m = p.matcher(f);
			if (m.find()) {			
				Cnb cnb = new Cnb();        	
    			cnb.setCnb(m.group(1).trim());			
    			cnbs.add(cnb);
			}
		}
		return cnbs;
	}

	@Override
	public String getISSNSeries() {
		// difficult do identify.. leaving with null
		return null;
	}
	
	@Override
	public String getISSNSeriesOrder() {
		// no way how to get.. leaving with null
		return null;
	}

	@Override
	public Long getWeight(Long baseWeight) {
		// leaving with null
		return null;
	}

	@Override
	public Long getScale() {
		// leaving with null
		return null;
	}

	@Override
	public String getUUId() {
		List<String> identifiers = dcRecord.getIdentifiers();
		String uuid = new String();
		
		Pattern p = DC_UUID_PATTERN;
		Matcher m;

		for (String f : identifiers) {
			m = p.matcher(f);
			if (m.find()) {			
				uuid = m.group(1).trim();
			}
		}
		if(uuid.isEmpty()) return null;
		else return uuid;
	}

	@Override
	public String getAuthorAuthKey() {
		// can't get authority key from DC .. leaving with null
		return null;
	}

	@Override
	public String getAuthorString() {
		return dcRecord.getFirstCreator();
	}

	@Override
	public String getClusterId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Oclc> getOclcs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Oclc> oclcs = new ArrayList<>();
		if (getLanguages().contains("cze")) return oclcs;
		Matcher matcher;
		
		for (String f : identifiers) {
			matcher = DC_OCLC_PATTERN.matcher(f);
			if (matcher.find()) {			
				Oclc oclc = new Oclc();        	
    			oclc.setOclcStr(matcher.group(1).trim());			
    			oclcs.add(oclc);
			}
		}
		return oclcs;
	}

	@Override
	public List<String> getLanguages() {
		return dcRecord.getLanguages().stream().filter(lang -> lang.equals("eng") || lang.equals("cze")).collect(Collectors.toList());
	}

	@Override
	public boolean matchFilter() {
		// implemented only in institution specific classes
		return true;
	}

	@Override
	public String getOAIRecordId() {
		// Nothing to return
		return null;
	}

	@Override
	public String getRaw001Id() {
		// Nothing to return
		return null;
	}

	@Override
	public Boolean isDeleted() {
		// Nothing to return
		return null;
	}

	@Override
	public CitationRecordType getCitationFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Ismn> getISMNs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Ismn> ismns = new ArrayList<>();
		Matcher matcher;
		Long ismnCounter = 0L;
		
		for (String f : identifiers) {
			matcher = DC_ISMN_PATTERN.matcher(f);
			if (matcher.find()) {
				Ismn ismn = new Ismn();
				String ismnStr = matcher.group(1).trim().replaceAll(ISMN_CLEAR_REGEX, "").replaceAll(ISMN10_PREFIX, ISMN13_PREFIX);
				try {
					if(ismnStr.length() != 13) throw new NumberFormatException();
					ismn.setIsmn(Long.valueOf(ismnStr));
					ismn.setOrderInRecord(++ismnCounter);
				} catch (NumberFormatException nfe) {
					logger.info(String.format("Invalid ISMN: %s", matcher.group(1).trim()));
					continue;
				}
				ismns.add(ismn);
			}
		}
		return ismns;
	}

	@Override
	public String getAuthorityId() {
		// can't get authority key from DC .. leaving with null
		return null;
	}

	@Override
	public List<String> getUrls() {
		return dcRecord.getUrls();
	}

	@Override
	public String filterSubjectFacet() {
		// implemented only in institution specific classes
		return null;
	}

	@Override
	public String getSourceInfo() {
		// Nothing to return
		return null;
	}

	@Override
	public List<Ean> getEANs() {
		// Nothing to return
		return Collections.emptyList();
	}

	@Override
	public List<ShortTitle> getShortTitles() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<String> getDefaultStatuses() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		// implemented in institution specific classes
		return null;
	}

}
