package cz.mzk.recordmanager.server.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.ISBNValidator;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Chars;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Ismn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.PublisherNumber;
import cz.mzk.recordmanager.server.model.ShortTitle;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.EANUtils;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class MetadataMarcRecord implements MetadataRecord {
	
	private static Logger logger = LoggerFactory.getLogger(MetadataMarcRecord.class);
	
	protected MarcRecord underlayingMarc;
	
	protected final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

	protected static final Pattern PAGECOUNT_PATTERN = Pattern.compile("(\\d+)");
	protected static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	protected static final Pattern ISBN_PATTERN = Pattern.compile("([\\dxX\\s\\-]*)(.*)");
	protected static final Pattern ISMN_PATTERN = Pattern.compile("([\\dM\\s\\-]*)(.*)");
	protected static final Pattern ISSN_PATTERN = Pattern.compile("(\\d{4}-\\d{3}[\\dxX])(.*)");
	protected static final Pattern EAN_PATTERN = Pattern.compile("([0-9]*)(.*)");
	protected static final Pattern SCALE_PATTERN = Pattern.compile("\\d+[ ^]*\\d+");
	protected static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");
	protected static final Pattern OCLC_PATTERN= Pattern.compile("(\\(ocolc\\))(.*)", Pattern.CASE_INSENSITIVE);
	protected static final Pattern PUBLISHER_NUMBER_PATTERN = Pattern.compile("([^\\W]*)");
	protected static final Pattern CPK0_PATTERN = Pattern.compile("cpk0");
	protected static final Pattern METAPROXY_TAG_PATTERN = Pattern.compile("[17]..");
	protected static final String ISBN_CLEAR_REGEX = "[^0-9Xx]";
	protected static final String ISMN_CLEAR_REGEX = "[^0-9M]";
	protected static final String NOTE_FORMAT = "\\(.+\\)";
	protected static final String BEGIN_BRACKET = "^\\(.*";
	protected static final String END_BRACKET = ".*\\)$";
	
	protected static final String ISMN10_PREFIX = "M";
	protected static final String ISMN13_PREFIX = "9790";
	
	protected static final Long MAX_PAGES = 10_000_000L;
	
	protected static final String DELETED_TAG = "YES";
	
	public MetadataMarcRecord(MarcRecord underlayingMarc) {
		if (underlayingMarc == null) {
			throw new IllegalArgumentException("Creating MetadataMarcRecord with NULL underlayingMarc.");
		}
		this.underlayingMarc = underlayingMarc;
	}
	
	@Override
	public String getUniqueId() {
		// TODO override this implementation in institution specific classes
		String id = underlayingMarc.getControlField("001");
		if (id == null) {
			id = underlayingMarc.getField("995", 'a');
		}
		return id;
	}

	
	
	@Override
	public List<Issn> getISSNs() {	
        List<Issn> issns = new ArrayList<Issn>();
        Long issnCounter = 0L;
        
        for(DataField field: underlayingMarc.getDataFields("022")){
        	Subfield subfieldA = field.getSubfield('a');
        	if(subfieldA == null){
        		continue;
        	}
        	Issn issn = new Issn();
        	
        	Matcher matcher = ISSN_PATTERN.matcher(subfieldA.getData());
			try {
				if(matcher.find()) {
					if(!issn.issnValidator(matcher.group(1))){
						throw new NumberFormatException();
					}					
					issn.setIssn(matcher.group(1));
					
					StringBuilder builder = new StringBuilder();
					if(matcher.group(2).trim() != null){ 
						String s = matcher.group(2).trim();
						if(s.matches(NOTE_FORMAT)) {
							builder.append(s.substring(1, s.length()-1));
						}
						else builder.append(s);
						builder.append(" ");
					}
					
					issn.setNote(builder.toString().trim());
					issn.setOrderInRecord(++issnCounter);
					issns.add(issn);
				}
				
			} catch (NumberFormatException e) {
				logger.info(String.format("Invalid ISSN: %s", subfieldA.getData()));
				continue;
			}
        
			
        }        
        
		return issns;
	}
	
	@Override
	public List<Cnb> getCNBs() {
		List<Cnb> cnbs = new ArrayList<Cnb>();
		
		for(DataField field: underlayingMarc.getDataFields("015")){
        	for(Subfield subfieldA: field.getSubfields('a')){
        		if(subfieldA != null){
        			Cnb cnb = new Cnb();        	
        			cnb.setCnb(subfieldA.getData());			
        			cnbs.add(cnb);
        		}
        	}
		}
        
		return cnbs;
	}

	@Override
	public String getISSNSeries() {
		String result = underlayingMarc.getField("490", 'x'); 
		if (result != null) {
			return result.substring(0, Math.min(result.length(), 300));
		}
		return null;
	}
	
	@Override
	public String getISSNSeriesOrder() {
		String result = underlayingMarc.getField("490", 'v'); 
		if (result != null) {
			return result.substring(0, Math.min(result.length(), 300));
		}
		return null;
	}
		

	@Override
	public Long getPageCount() {		
		String count = underlayingMarc.getField("300", 'a');
		if(count == null){
			return null;
		}
		
		Long maxPages = -1L;
		Matcher matcher = PAGECOUNT_PATTERN.matcher(count);
		while (matcher.find()) {
			try {
				Long pages = Long.parseLong(matcher.group(0));
				maxPages = pages > maxPages ? pages : maxPages;
			} catch (NumberFormatException e) {}
		}

		if (maxPages < 1L) {
			return null;
		}

		return maxPages < MAX_PAGES ? maxPages : MAX_PAGES;
	}
	
	@Override
	public List<Isbn> getISBNs() {
		List<Isbn> isbns = new ArrayList<Isbn>();
		Long isbnCounter = 0L;

		for(DataField field: underlayingMarc.getDataFields("020")){
			Subfield subfieldA = field.getSubfield('a');
			if (subfieldA == null) {
				continue;
			}
			
			Isbn isbn = new Isbn();

			Matcher matcher = ISBN_PATTERN.matcher(subfieldA.getData());

			if (matcher.find()) {
				String g1 = matcher.group(1);
				if (g1 == null) {
					continue;
				}
				String isbnStr = isbnValidator.validate(g1.replaceAll(ISBN_CLEAR_REGEX,"").replaceAll("x", "X"));
				try {
					if (isbnStr == null) {
						throw new NumberFormatException();
					}
					Long isbn13 = Long.valueOf(isbnStr);
					isbn.setIsbn(isbn13);
				} catch (NumberFormatException nfe) {
					logger.info(String.format("Invalid ISBN: %s", subfieldA.getData()));
					continue;
				}
			}

			
			StringBuilder builder = new StringBuilder();
			if(matcher.group(2).trim() != null){ 
				String s = matcher.group(2).trim();
				if(s.matches(NOTE_FORMAT)) {
					builder.append(s.substring(1, s.length()-1));
				}
				else builder.append(s);
				builder.append(" ");
			}
			for(Subfield subfieldQ: field.getSubfields('q')){
				if(subfieldQ.getData().matches(NOTE_FORMAT)) {
					builder.append(subfieldQ.getData().substring(1, subfieldQ.getData().length()-1));
				}
				else builder.append(subfieldQ.getData());
				builder.append(" ");
			}
			isbn.setNote(builder.toString().trim());
			isbn.setOrderInRecord(++isbnCounter);
			isbns.add(isbn);
		}
		
		return isbns;
	}

	/**
	 * get publication year from fields 264c, 260c or 008
	 */
	@Override
	public Long getPublicationYear() {
		
		String year = underlayingMarc.getField("264", 'c');
		if (year == null) {
			year = underlayingMarc.getField("260", 'c');
		}
		if (year == null) {
			year = underlayingMarc.getControlField("008");
			if (year == null || year.length() < 12) {
				return null;
			}
			year = year.substring(7, 11);
		}

		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {}
		return null;
	}
		
	/**
	 * get title of record
	 * 
	 * @return all 245a:245b.245n.245p and 240a:240b.240n.240p. If no title is
	 *         found, list containing empty string is returned
	 */
	@Override
	public List<Title> getTitle() {
		final char titleSubfields[] = new char[]{'a','b','n','p'};
		List<Title> result = new ArrayList<Title>();
		
		Long titleOrder = 0L;
		for (String key: new String[]{"245", "240"}) {
			for (DataField field :underlayingMarc.getDataFields(key)) {
				StringBuilder builder = new StringBuilder();
				
				for(Subfield subfield: field.getSubfields()){
					if (MetadataUtils.hasTrailingPunctuation(builder.toString())) {
						builder.append(" ");
					}
					if(Chars.contains(titleSubfields, subfield.getCode())){
						builder.append(subfield.getData());
					}
				}
				
				if (builder.length() > 0) {
					Title title = new Title();
					title.setTitleStr(builder.toString());
					title.setOrderInRecord(++titleOrder);
					title.setSimilarityEnabled(MetadataUtils.similarityEnabled(field, title));
					result.add(title);
				}
			}
		}

		return result;
	}

	@Override
	public String export(IOFormat iOFormat) {
		return underlayingMarc.export(iOFormat);
	}

	
	protected boolean isBook(){		
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		String ldr07 = Character.toString(underlayingMarc.getLeader().getImplDefined1()[0]);
		
		String f006 = underlayingMarc.getControlField("006");
	    String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		if(ldr06.matches("(?i)[at]") && ldr07.matches("(?i)[cdm]"))	return true;				
		if(f006_00.matches("(?i)[a]")) return true;
		
		return false;		
	}
	
	protected boolean isPeriodical(){
		String ldr07 = Character.toString(underlayingMarc.getLeader().getImplDefined1()[0]);
		
		if(ldr07.matches("(?i)[is]")) return true;		
		
		return false;		
	}
	
	protected boolean isArticle(){
		String ldr07 = Character.toString(underlayingMarc.getLeader().getImplDefined1()[0]);
		
		if(ldr07.matches("(?i)[ab]")) return true;		
		
		return false;		
	}

	protected boolean isArticle773() {
		return !underlayingMarc.getDataFields("773").isEmpty();
	}

	protected boolean isMap(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		if(ldr06.matches("(?i)[ef]")) return true;
		if(f006_00.matches("(?i)[ef]")) return true;
		if(f245h.matches("(?i).*kartografický\\sdokument.*")) return true;
		if(f007_00.matches("(?i)a")) return true;
		if(f336b.matches("(?i)cr.*")) return true;
		
		return false;		
	}
	
	protected boolean isMusicalScores(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";

		if(ldr06.matches("(?i)[cd]"))return true;
		if(f006_00.matches("(?i)[cd]") && f245h.matches("(?i).*hudebnina.*")){
			return true;
		}
		if(f336b.equalsIgnoreCase("tcm")) return true;
		if(f336b.equalsIgnoreCase("ntm")) return true;
		if(f336b.equalsIgnoreCase("ntv")) return true;
		if(f336b.equalsIgnoreCase("tcn")) return true;
		
		return false;		
	}
	
	protected boolean isVisualDocument(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
		
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
		if(ldr06.matches("(?i)[kg]")) return true;		
		if(f007_00.matches("(?i)[kg]")) return true;
		if(f245h.matches("(?i).*grafika.*")) return true;
		if(f006_00.matches("(?i)[kg]")) return true;
		if(f336b.matches("(?i)sti|tci|cri|crt")) return true;
		if(f337b.matches("(?i)g")) return true;
		if(f338b.matches("(?i)g.*")) return true;		
		
		return false;		
	}
	
	protected boolean isMicroform(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
				
		String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		String f008 = underlayingMarc.getControlField("008");
	    String f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toString(f008.charAt(23)) : "";
	    String f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toString(f008.charAt(29)) : "";

		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
		
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
		if(ldr06.matches("(?i)[acdpt]") && f008_23.matches("(?i)[abc]")) return true;
		if(ldr06.matches("(?i)[efk]") && f008_29.matches("(?i)b")) return true;
		if(f007_00.matches("(?i)h")) return true;
		if(f245h.matches("(?i).*mikrodokument.*")) return true;
		if(f337b.matches("(?i)h")) return true;
		if(f338b.matches("(?i)h.*")) return true;
		
		return false;
	}
		
	protected boolean isBraill(){
		String f007 = underlayingMarc.getControlField("007");
		String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		String f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toString(f007.charAt(1)) : "";

		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";

		if(f007_00.matches("(?i)f") && f007_01.matches("(?i)b") && f245h.matches("(?i).*hmatové\\spísmo.*")){
			return true;
		}
		
		if(f007_00.matches("(?i)t") && f007_01.matches("(?i)c")){
			return true;
		}
		if(f336b.matches("(?i)tct|tcm|tci|tcf")) return true;
		
		return false;
	}
	
	protected boolean isElectronicSource() {
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		String f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toString(f006.charAt(6)) : "";
		
		String f008 = underlayingMarc.getControlField("008");
		String f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toString(f008.charAt(23)) : "";
		String f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toString(f008.charAt(29)) : "";
		
		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";
		
		if (ldr06.matches("(?i)[acdijpt]") && f008_23.matches("(?i)[oq]")) {
			return true;
		}
		if (f006_00.matches("(?i)[acdijpt]") && f006_06.matches("(?i)[oq]")) {
			return true;
		}
		if (ldr06.matches("(?i)[efgkopr]") && f008_29.matches("(?i)[oq]")) {
			return true;
		}
		if (f006_00.matches("(?i)[efgkopr]") && f006_06.matches("(?i)[oq]")) {
			return true;
		}
		if (f338b.matches("(?i)cr")) return true;
		
		return false;
	}

	protected boolean isComputerCarrier(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		String f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toString(f006.charAt(6)) : "";
		
		String f007 = underlayingMarc.getControlField("007");
		String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		String f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toString(f007.charAt(1)) : "";
		
		String f008 = underlayingMarc.getControlField("008");
		String f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toString(f008.charAt(23)) : "";
		String f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toString(f008.charAt(29)) : "";

		String f245h = underlayingMarc.getField("245", 'h');
		if(f245h == null) f245h = "";
		
		String f300a = underlayingMarc.getField("300", 'a');
		if(f300a == null) f300a = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
		if(f245h.matches("(?i).*elektronický\\szdroj.*")){
			return true;
		}
		if(ldr06.matches("(?i)[acdijpt]") && f008_23.matches("(?i)s")){
			return true;
		}
		if(f006_00.matches("(?i)[acdijpt]") && f006_06.matches("(?i)s")){
			return true;
		}
		if(ldr06.matches("(?i)[efgkopr]") && f008_29.matches("(?i)s")){
			return true;
		}
		if(f006_00.matches("(?i)[efgkopr]") && f006_06.matches("(?i)s")){
			return true;
		}
		if(ldr06.matches("(?i)m") && f006_00.matches("(?i)m")){
			return true;
		}
		if(ldr06.matches("(?i)m") && f245h.matches("(?i).*multim[eé]dium.*") && f300a.matches("(?i).*cd-rom.*")) return true;
		if(f007_00.matches("(?i)c") && !f007_01.matches("(?i)r")) return true;
		if(f300a.matches("(?i).*disketa.*")) return true;
		if(f336b.matches("(?i)cod|cop")) return true;
		if(f338b.matches("(?i)ck|cb|cd|ce|ca|cf|ch|cz")) return true;
		
		return false;
	}
	
	protected HarvestedRecordFormatEnum getAudioFormat(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
	    String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
	    String f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toString(f007.charAt(1)) : "";
	    	    
		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f300 = underlayingMarc.getDataFields("300").toString();
		String f500 = underlayingMarc.getDataFields("500").toString();
		
		String f300a = underlayingMarc.getField("300", 'a');
		if(f300a == null) f300a = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
				
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
				
		// AUDIO_CD
		for (String data : new String[]{f300, f500}) {
			if(data.matches("(?i).*kompaktn[ií](ch)?\\sd[ei]sk(ů)?.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		}
		if(f300.matches("((?i).*zvukov[eéaá])\\sCD.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		if(f300a.matches(".*CD.*")) {
			if(!f300a.matches(".*CD-ROM.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		}
		for (String data : new String[]{f300, f500}) {
			if(data.matches(".*CD-R.*") && !data.matches(".*CD-ROM.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		}
		if(f300.matches("(?i).*zvukov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*") && f300.matches("(?i).*(digital|12\\s*cm).*")) return HarvestedRecordFormatEnum.AUDIO_CD;
	
		// AUDIO_LP
		if(f300.matches("(?i).*gramofonov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300.matches("(?i).*zvukov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*") && f300.matches("(?i).*analog.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300a.matches(".*LP.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300a.matches(".*SP.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		
		// AUDIO_CASSETTE
		if(f007_00.matches("(?i)s") && f338b.matches("(?i)ss")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
		if(f007_00.matches("(?i)s") && f007_01.matches("(?i)[zgeiqt]")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
		if(f300.matches("(?i).*zvukov(a|á|e|é|ych|ých)\\skaze(ta|ty|t).*")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
		if(f300.matches("(?i).*(mc|kz|mgk).*")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
		if(f300.matches("(?i).*magnetofonov(a|á|e|é|ych|ých)\\skaze(ta|ty|t).*")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
		
		// AUDIO_OTHER
		if(ldr06.matches("(?i)[ij]")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f007_00.matches("(?i)s")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f245h.matches("(?i).*zvukový\\száznam.*")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f337b.matches("(?i)s")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f006_00.matches("(?i)[ij]")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f338b.matches("(?i)s.*")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f007_00.matches("(?i)i")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		if(f336b.matches("(?i)spw|snd")) return HarvestedRecordFormatEnum.AUDIO_OTHER;
		
		return null;
	}
	
	protected boolean isAudioDVD() {
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f300a = underlayingMarc.getField("300", 'a');
		if(f300a == null) f300a = "";
		
		// AUDIO_DVD
		if(ldr06.matches("(?i)[ij]") && f300a.matches("(?i).*dvd.*")) return true;
		
		return false;
	}
	
	protected HarvestedRecordFormatEnum getVideoDocument(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
	    String f006_16 = (f006 != null) && (f006.length() > 16) ? Character.toString(f006.charAt(16)) : "";
		
		String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
	    String f007_04 = (f007 != null) && (f007.length() > 4) ? Character.toString(f007.charAt(4)) : "";
		
	    String f008 = underlayingMarc.getControlField("008");
	    String f008_33 = (f008 != null) && (f008.length() > 33) ? Character.toString(f008.charAt(33)) : "";
	    
	    String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f300 = underlayingMarc.getDataFields("300").toString();
		
		String f300a = underlayingMarc.getField("300", 'a');
		if(f300a == null) f300a = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
		
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
		String f500 = underlayingMarc.getDataFields("500").toString();
		
		// Bluray
		if(ldr06.matches("(?i)g") && f300.matches("(?i).*blu.*ray.*")) return HarvestedRecordFormatEnum.VIDEO_BLURAY;
		
		// VHS
		if(f300.matches("(?i).*vhs.*")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		if(f007_00.matches("(?i)v") && f007_04.matches("(?i)b")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		if(f300a.matches("(?i).*videokazet[ay]?.*")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		
		// DVD
		if(f007_00.matches("(?i)v") && f007_04.matches("(?i)v")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		if(f300a.matches(".*DVD[ -]?vide[oa].*")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		if (f300.matches("(?i).*videodisk.*")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		if (f500.matches("(?i).*videodisk.*")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		if (f338b.matches("(?i)vd")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		
		// CD
		if(ldr06.matches("(?i)g") && f300a.matches("(?i).*cd.*")) return HarvestedRecordFormatEnum.VIDEO_CD;
		
		// others
		if(f007_00.matches("(?i)[vm]")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(f245h.matches("(?i).*videozáznam.*")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(f337b.matches("(?i)v")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(ldr06.matches("(?i)g") && f008_33.matches("(?i)[mv]")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(f006_00.matches("(?i)g") && f006_16.matches("(?i)[mv]")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(f338b.matches("(?i)v.*")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		if(f336b.matches("(?i)tdi|tdm")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		
		if(f338b.matches("(?i)vr|vz|vc|mc|mf|mr|mo|mz")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
		
		return null;
	}
	
	protected boolean isVideoDVD() {
		String f300a = underlayingMarc.getField("300", 'a');
		if(f300a == null) f300a = "";
		
		// VIDEO_DVD
		if(f300a.matches("(?i).*dvd.*")) return true;
		
		return false;

	}
	
	protected boolean isOthers(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f007 = underlayingMarc.getControlField("007");
		String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		String f008 = underlayingMarc.getControlField("008");
		String f008_33 = (f008 != null) && (f008.length() > 33) ? Character.toString(f008.charAt(33)) : "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
				
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
		if(ldr06.matches("(?i)o")) return true;
		if(f006_00.matches("(?i)o") && f007_00.matches("(?i)o")) return true;
		
		if(ldr06.matches("(?i)p")) return true;

		if(ldr06.matches("(?i)r")) return true;
		if(f336b.matches("(?i)tcf|tdm|tdf")) return true;
		if(f008_33.matches("(?i)d")) return true;
		if(f006_00.matches("(?i)r")) return true;
		
		if(f007_00.matches("(?i)z") && f336b.matches("(?i)zzz")) return true;
		if(f337b.matches("(?i)[xz]")) return true;
		if(f338b.matches("(?i)zu")) return true;
		
		return false;
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<HarvestedRecordFormatEnum>();
		
		if(isBook()) hrf.add(HarvestedRecordFormatEnum.BOOKS);
		if(isPeriodical()) hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		if(isArticle()) hrf.add(HarvestedRecordFormatEnum.ARTICLES);
		if (isArticle773()) return Collections.singletonList(HarvestedRecordFormatEnum.ARTICLES);
		if(isMap()) hrf.add(HarvestedRecordFormatEnum.MAPS);
		if(isMusicalScores()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if(isVisualDocument()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if(isMicroform()) hrf.add(HarvestedRecordFormatEnum.OTHER_MICROFORMS);
		if(isBraill()) hrf.add(HarvestedRecordFormatEnum.OTHER_BRAILLE);
		HarvestedRecordFormatEnum audio = getAudioFormat();
		if (audio != null) {
			if (isAudioDVD()) hrf.add(HarvestedRecordFormatEnum.AUDIO_DVD);
			else hrf.add(audio);
		}
		HarvestedRecordFormatEnum video = getVideoDocument();
		if (video != null) {
			if (isVideoDVD()) {
				if (!hrf.contains(HarvestedRecordFormatEnum.VIDEO_DVD)) hrf.add(HarvestedRecordFormatEnum.VIDEO_DVD);
			}
			else hrf.add(video);
		}
		if(isComputerCarrier()) hrf.add(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER);
		if(isOthers()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		if(hrf.isEmpty()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		
		return hrf;
	}

	@Override
	public Long getScale() {
		String scaleStr = underlayingMarc.getField("255", 'a');
		if (scaleStr == null) {
			return null;
		}
		Matcher matcher = SCALE_PATTERN.matcher(scaleStr);
		if (matcher.find()) {
			String strValue = matcher.group(0).replaceAll("[ ^]+", "");
			try {
				return Long.valueOf(strValue);
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getUUId() {
		String baseStr = underlayingMarc.getField("856", 'u');
		if (baseStr == null) {
			return null;
		}

		Matcher matcher = UUID_PATTERN.matcher(baseStr);
		if (matcher.find()) {
			String uuidStr = matcher.group(0);
			if (uuidStr != null && uuidStr.length() > 5) {
				return uuidStr.substring(5);
			}
		}
		return null;
	}

    @Override
	public Long getWeight(Long baseWeight) {
		Long weight = 0L;
		if(baseWeight != null) weight = baseWeight;
		
		String[] fields1xx = new String[]{"100", "110", "111", "130"};
		String[] fields6xx = new String[]{"600", "610", "611", "630", "648", "650", "651", "653", "654", "655",
				"656", "657", "658", "662", "690", "691", "692", "693", "694", "695", "696", "697", "698", "699"};
		String[] fields7xx = new String[]{"700", "710", "711", "720", "730", "740", "751", "752", "753", "754", "760", 
				"762", "765", "767", "770", "772", "773", "774", "775", "776", "777", "780", "785", "786", "787"};
		
		if(underlayingMarc.getDataFields("245").isEmpty()){
			return 0L;
		}

		String ldr17 = Character.toString(underlayingMarc.getLeader().getImplDefined2()[0]);
		if(ldr17.matches("1")) weight -= 1;
		else if(ldr17.matches("[2-4]")) weight -= 2;
		else if(ldr17.matches("[05-9]")) weight -= 3;		
		if(underlayingMarc.getControlField("008") == null) weight -= 1;
		if(underlayingMarc.getDataFields("300").isEmpty()) weight -= 1;
		
		boolean exists1xx = false;
		for (String key: fields1xx){
			if(!underlayingMarc.getDataFields(key).isEmpty()){
				exists1xx = true;
				break;
			}
		}
		boolean f245Ind1 = false;
		for(DataField dataField: underlayingMarc.getDataFields("245")){
			if(dataField.getIndicator1() == 0) f245Ind1 = true;
		}
		
		if(!exists1xx && !f245Ind1){
			weight -= 1;
		}
		
		if(!underlayingMarc.getDataFields("080").isEmpty() || !underlayingMarc.getDataFields("072").isEmpty()){
			weight += 1;
		}
		if(!underlayingMarc.getDataFields("964").isEmpty()) weight += 1;
		else{
			for(String key: fields6xx){
				if(!underlayingMarc.getDataFields(key).isEmpty()){
					weight += 1;
					break;
				}
			}
		}
		
		if(!getISBNs().isEmpty() || !getISSNs().isEmpty() || !getCNBs().isEmpty()){
			weight += 1;
		}
		
		boolean exist7in1xx = false;
		for(String key: fields1xx){
			if(underlayingMarc.getField(key, '7') != null){
				weight += 1;
				exist7in1xx = true;
				break;
			}
		}
		if(!exist7in1xx){
			for(String key: fields7xx){
				if(underlayingMarc.getField(key, '7') != null){
					weight += 1;
					break;
				}
			}
		}
		
		for(String subfield: underlayingMarc.getFields("040", 'e')){
			if(subfield.matches("(?i)rda")){
				weight += 1;
				break;
			}
		}
		
		return weight;
	}

	@Override
	public String getAuthorAuthKey() {
		String f100s7 = underlayingMarc.getField("100", '7');
		if(f100s7 != null){
			return f100s7;
		}
		
		String f700s7 = underlayingMarc.getField("700", '7');
		if(f700s7 != null){
			return f700s7;
		}
		return null;
		
	}

	@Override
	public String getAuthorString() {
		String f100a = underlayingMarc.getField("100", 'a');
		if(f100a != null){
			return f100a;
		}
		
		String f700a = underlayingMarc.getField("700", 'a');
		if(f700a != null){
			return f700a;
		}
		return null;
	}

	@Override
	public String getClusterId() {
		// implemented only in selected specialization
		return null;
	}

	@Override
	public List<Oclc> getOclcs() {
		List<Oclc> result = new ArrayList<>();
		if (getLanguages().contains("cze")) return result;
		for (DataField df: underlayingMarc.getDataFields("035")) {
			Subfield subA = df.getSubfield('a');
			if (subA == null) {
				continue;
			}
			
			Matcher matcher = OCLC_PATTERN.matcher(subA.getData());
			if (matcher.matches() && matcher.groupCount() >= 2) {
				Oclc oclc = new Oclc();
				oclc.setOclcStr(matcher.group(2));
				result.add(oclc);
			}
			
		}
		return result;
	}

	@Override
	public List<String> getLanguages() {
		Set<String> result = new HashSet<>();
		for (DataField df: underlayingMarc.getDataFields("041")) {
			for (Subfield subA: df.getSubfields('a')) {
				String lang = null;
				if (subA.getData().toLowerCase().equals("cze")) {
					lang = "cze";
				} else if (subA.getData().toLowerCase().equals("eng")) {
					lang = "eng"; 
				} else {
					lang = "oth";
				}
				result.add(lang);
			}
		}
		
		if (result.isEmpty()) {
			String cf = underlayingMarc.getControlField("008");
			if (cf != null && cf.length() > 39) {
				String substr = cf.substring(35, 38);
				String lang = null;
				if (substr.toLowerCase().equals("cze")) {
					lang = "cze";
				} else if (substr.toLowerCase().equals("eng")) {
					lang = "eng"; 
				}
				if (lang != null) {
					result.add(lang);
				}
			}
		}
		
		return new ArrayList<String>(result);
	}

	@Override
	public boolean matchFilter() {
		if (underlayingMarc.getDataFields("245").isEmpty()) return false;
		for (DataField df : underlayingMarc.getDataFields("914")) {
			if (df.getSubfield('a') != null
					&& CPK0_PATTERN.matcher(df.getSubfield('a').getData()).matches()) return false;
		}
		// more rules in institution specific classes
		return true;
	}

	@Override
	public String getOAIRecordId() {
		String oai = underlayingMarc.getField("OAI", 'a');
		if(oai != null) return oai;
		return null;
	}

	@Override
	public String getRaw001Id() {
		return underlayingMarc.getControlField("001");
	}

	@Override
	public Boolean isDeleted() {
		String deleted = underlayingMarc.getField("DEL", 'a');
		if(deleted != null && deleted.equals(DELETED_TAG)) return true;
		else return false;
	}
	
	@Override
	public CitationRecordType getCitationFormat(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord()).toLowerCase();
		String ldr07 = Character.toString(underlayingMarc.getLeader().getImplDefined1()[0]).toLowerCase();
		
		Boolean exists85641 = false;
		for(DataField df: underlayingMarc.getDataFields("856")){
			if(df.getIndicator1() == '4' && df.getIndicator2() == '1'){
				exists85641 = true;
			}
		}
		
		if(!underlayingMarc.getDataFields("502").isEmpty()){
			return CitationRecordType.ACADEMIC_WORK;
		}
		
		if(ldr06.matches("[at]") && ldr07.matches("[cdm]")){
			if(exists85641) return CitationRecordType.ELECTRONIC_BOOK;
			else return CitationRecordType.BOOK;
		}
		
		if(ldr07.matches("[is]")){
			if(exists85641) return CitationRecordType.ELECTRONIC_PERIODICAL;
			else return CitationRecordType.PERIODICAL;
		}
		
		Boolean matches = false;
		for(DataField df: underlayingMarc.getDataFields("773")){
			if(df.toString().matches("(?i).*sborník.*|.*proceedings.*|.*almanach.*")){
				matches = true;
			}
		}
		if(matches){
			if(exists85641) return CitationRecordType.ELECTRONIC_CONTRIBUTION_PROCEEDINGS;
			else return CitationRecordType.CONTRIBUTION_PROCEEDINGS;
		}
		
		if(ldr07.matches("[ab]")){
			if(exists85641) return CitationRecordType.ELECTRONIC_ARTICLE;
			else return CitationRecordType.ARTICLE;
		}
		
		if(ldr06.matches("[ef]")) return CitationRecordType.MAPS;
		
		if(ldr06.matches("[cdkgijopr]")) return CitationRecordType.OTHERS;
		
		return CitationRecordType.ERROR;
	}
	
	@Override
	public List<String> getBarcodes(){
		return underlayingMarc.getFields("996", 'b');
	}

	@Override
	public List<Ismn> getISMNs() {
		List<Ismn> ismns = new ArrayList<>();
		Long ismnCounter = 0L;
		
		for(DataField df: underlayingMarc.getDataFields("024")){
			Subfield sfA = df.getSubfield('a');
			if((df.getIndicator1() == '2') && (sfA != null)){
				
				Matcher matcher = ISMN_PATTERN.matcher(sfA.getData());
				if(!matcher.find()) continue;
				String g1 = matcher.group(1); // ismn
				if (g1 == null)	continue;				
				
				Ismn ismn = new Ismn();
				String ismnStr = g1.replaceAll(ISMN_CLEAR_REGEX, "").replaceAll(ISMN10_PREFIX, ISMN13_PREFIX);
				try {
					if(ismnStr.length() != 13) throw new NumberFormatException();
					ismn.setIsmn(Long.valueOf(ismnStr));
				} catch (NumberFormatException nfe) {
					logger.info(String.format("Invalid ISMN: %s", sfA.getData()));
					continue;
				}
				
				StringBuilder builder = new StringBuilder();
				String g2 = matcher.group(2).trim();
				if(g2 != null){ 
					builder.append(trimNote(g2));
					builder.append(" ");
				}
				
				for(Subfield sfQ: df.getSubfields('q')){
					if(sfQ == null) continue;
					builder.append(trimNote(sfQ.getData()));
					builder.append(" ");
				}
				ismn.setNote(builder.toString().trim());
				ismn.setOrderInRecord(++ismnCounter);
				ismns.add(ismn);
			}
		}
		
		return ismns;
	}
	
	protected String trimNote(String note){
		int beginIndex = 0;
		int endIndex = note.length();
		if(note.matches(BEGIN_BRACKET)) beginIndex = 1;
		if(note.matches(END_BRACKET)) --endIndex;
		if(beginIndex <= endIndex) {
			return note.substring(beginIndex, endIndex);
		}
		else return note;
	}

	@Override
	public String getAuthorityId() {
		// implemented only in institution specific classes
		return null;
	}
	
	@Override
	public List<String> getUrls(){
		return getUrls(Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
	}
	
	protected List<String> getUrls(String availability) {
    	List<String> result = new ArrayList<>();
    	
    	for (DataField df: underlayingMarc.getDataFields("856")) {
    		if (df.getSubfield('u') == null) {
    			continue;
    		}
    		String link = df.getSubfield('u').getData();
    		String comment = "";
    		
    		String sub3 = null,subY = null,subZ = null;
    		
    		if (df.getSubfield('3') != null) {
    			sub3 = df.getSubfield('3').getData();
    		}
    		if (df.getSubfield('y') != null) {
    			subY = df.getSubfield('y').getData();
    		}
    		if (df.getSubfield('z') != null) {
    			subZ = df.getSubfield('z').getData();
    		}
    		
    		if (sub3 != null) {
    			comment = sub3;
    			if (subZ != null) {
    				comment += " (" + subZ + ")";
    			}
    		} else if (subY != null) {
    			comment = subY;
    			if (subZ != null) {
    				comment += " (" + subZ + ")";
    			}
    		} else if (subZ != null) {
    			comment = subZ;
    		}
    		
    		result.add(availability + "|" + link + "|" + comment);
    	}
    	
    	return result;
    }

	@Override
	public String getPolicyKramerius() {
		// Nothing to return
		return null;
	}

	@Override
	public String filterSubjectFacet() {
		// implemented only in institution specific classes
		return null;
	}

	@Override
	public List<Ean> getEANs() {
		List<Ean> results = new ArrayList<>();
		Long eanCounter = 0L;
		for (DataField df: underlayingMarc.getDataFields("024")) {
			if (df.getIndicator1() == '3' && df.getSubfield('a') != null) {
				Matcher matcher = EAN_PATTERN.matcher(df.getSubfield('a').getData());
				
				if (matcher.find()) {
					String g1 = matcher.group(1);
					if (g1 == null) continue;
					Ean ean = new Ean();
					try {
						if (EANUtils.isEAN13valid(g1)) {
							ean.setEan(Long.valueOf(g1));
						}
						else throw new NumberFormatException();
					} catch (NumberFormatException nfe) {
						logger.info(String.format("Invalid EAN: %s", df.getSubfield('a').getData()));
						continue;
					}
					
					ean.setNote(parseNote(matcher.group(2), df.getSubfields('q')));
					ean.setOrderInRecord(++eanCounter);
					results.add(ean);
				}
			}
		}
		
		return results;
	}
	
	protected String parseNote(String note, List<Subfield> sfq) {
		StringBuilder builder = new StringBuilder();
		if(note.trim() != null){ 
			String s = note.trim();
			if(s.matches(NOTE_FORMAT)) {
				builder.append(s.substring(1, s.length()-1));
			}
			else builder.append(s);
			builder.append(" ");
		}
		for(Subfield subfieldQ: sfq){
			if(subfieldQ.getData().matches(NOTE_FORMAT)) {
				builder.append(subfieldQ.getData().substring(1, subfieldQ.getData().length()-1));
			}
			else builder.append(subfieldQ.getData());
			builder.append(" ");
		}
		
		return builder.toString().trim();
	}

	@Override
	public List<ShortTitle> getShortTitles() {
		List<ShortTitle> results = new ArrayList<>();
		Long shortTitleCounter = 0L;
		char[] shortTitleSf = new char[]{'a', 'n', 'p'};
		
		for (String tag: new String[]{"245", "240"}) {
			for (DataField df :underlayingMarc.getDataFields(tag)) {
				if (df.getSubfield('b') == null) continue;
				
				StringBuilder builder = new StringBuilder();
				for(Subfield subfield: df.getSubfields()){
					if (MetadataUtils.hasTrailingPunctuation(builder.toString())) {
						builder.append(" ");
					}
					if(Chars.contains(shortTitleSf, subfield.getCode())){
						builder.append(subfield.getData());
					}
				}

				if (builder.length() > 0) {
					ShortTitle shortTitle = new ShortTitle();
					shortTitle.setShortTitleStr(builder.toString());
					shortTitle.setOrderInRecord(++shortTitleCounter);
					shortTitle.setSimilarityEnabled(MetadataUtils.similarityEnabled(df, shortTitle));
					results.add(shortTitle);
				}
			}
		}

		return results;
	}

	@Override
	public List<String> getDefaultStatuses() {
		// implemented in institution specific classes
		return Collections.emptyList();
	}

	@Override
	public List<String> getInternationalPatentClassfication() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public Boolean getMetaproxyBool() {
		Set<String> fieldTags = underlayingMarc.getAllFields().keySet();
		if (underlayingMarc.getControlField("008") == null) return false;
		if (!fieldTags.contains("300") || !fieldTags.contains("245")
				|| (!fieldTags.contains("260") && !fieldTags.contains("264"))) return false;
		boolean f245ind1 = false;
		for (DataField df : underlayingMarc.getDataFields("245")) {
			if (df.getIndicator1() == '0') f245ind1 = true;
		}
		if (!f245ind1) {
			boolean f1xxOr7xx = false;
			for (String tag : fieldTags) {
				if (METAPROXY_TAG_PATTERN.matcher(tag).matches()) {
					f1xxOr7xx = true;
					break;
				}
			}
			if (!f1xxOr7xx) return false;
		}
		return true;
	}

	@Override
	public List<PublisherNumber> getPublisherNumber() {
		List<PublisherNumber> results = new ArrayList<>();
		Long i = 0L;
		for (DataField df : underlayingMarc.getDataFields("028")) {
			if (df.getIndicator1() == '0' && df.getSubfield('a') != null) {
				String result = df.getSubfield('a').getData().toLowerCase().replaceAll("\\W", "");
				results.add(new PublisherNumber(result, ++i));
			}
		}
		return results;
	}

	@Override
	public String getSourceInfoX() {
		return underlayingMarc.getField("773", 'x');
	}

	@Override
	public String getSourceInfoT() {
		return underlayingMarc.getField("773", 't');
	}

	@Override
	public String getSourceInfoG() {
		return underlayingMarc.getField("773", 'g');
	}

	protected String generateSfxUrl(String url, String id, Map<String, String> specificParams) {
		Map<String, String> allParams = new HashMap<>();
		allParams.put("url_ver", "Z39.88-2004");
		allParams.put("sfx.ignore_date_threshold", "1");
		allParams.put("rft.object_id", id);

		allParams.putAll(specificParams);
		return UrlUtils.buildUrl(url, allParams);
	}

	protected String getSfxInstitute() {
		return getSfxInstitute(Collections.emptyMap());
	}

	protected String getSfxInstitute(Map<String, String> sfx_map) {
		String prefix = underlayingMarc.getControlField("003");
		prefix = prefix.toUpperCase();
		if (sfx_map.containsKey(prefix)) {
			prefix = sfx_map.get(prefix);
		}
		return prefix;
	}

	protected boolean isIrelView() {
		return !isBraill() && !isMusicalScores() && !isVisualDocument();
	}

}
