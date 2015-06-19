package cz.mzk.recordmanager.server.metadata;

import java.util.ArrayList;
import java.util.List;
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
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class MetadataMarcRecord implements MetadataRecord {

	private static Logger logger = LoggerFactory.getLogger(MetadataMarcRecord.class);
	
	protected MarcRecord underlayingMarc;
	
	protected final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

	protected static final Pattern PAGECOUNT_PATTERN = Pattern.compile("\\d+");
	protected static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	protected static final Pattern ISBN_PATTERN = Pattern.compile("([\\dxX\\s\\-]*)(.*)");
	protected static final Pattern ISSN_PATTERN = Pattern.compile("(\\d{4}-\\d{3}[\\dxX])(.*)");
	protected static final Pattern SCALE_PATTERN = Pattern.compile("\\d+[\\ \\^]*\\d+");
	protected static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");
	protected static final String ISBN_CLEAR_REGEX = "[^0-9^X^x]";
	
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
						if(s.matches("\\(.+\\)")) {
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
		return underlayingMarc.getField("490", 'x');
	}
	
	@Override
	public String getISSNSeriesOrder() {
		return underlayingMarc.getField("490", 'v');
	}
		

	@Override
	public Long getPageCount() {		
		String count = underlayingMarc.getField("300", 'a');
		if(count == null){
			return null;
		}	
		
		Matcher matcher = PAGECOUNT_PATTERN.matcher(count);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {}
		return null;
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
				if(s.matches("\\(.+\\)")) {
					builder.append(s.substring(1, s.length()-1));
				}
				else builder.append(s);
				builder.append(" ");
			}
			for(Subfield subfieldQ: field.getSubfields('q')){
				if(subfieldQ.getData().matches("\\(.+\\)")) {
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
					result.add(title);
				}
			}
		}
		
		if (result.isEmpty()) {
			Title title = new Title();
			title.setTitleStr("");
			title.setOrderInRecord(1L);
			result.add(title);
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
		
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		if(ldr06.matches("(?i)[at]") && ldr07.matches("(?i)[cdm]"))	return true;		
		if(f007_00.matches("(?i)t")) return true;		
		if(f006_00.matches("(?i)[at]")) return true;
		
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
	
	protected boolean isManuscript(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
				
		if(ldr06.matches("(?i)[tdf]")) return true;
		if(f006_00.matches("(?i)[tdf]")) return true;
		if(f245h.matches("(?i).*rukopis.*")) return true;
		
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
	
	protected boolean isLargePrint () {
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
	    String f006 = underlayingMarc.getControlField("006");
		String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		String f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toString(f006.charAt(6)) : "";
	    String f006_12 = (f006 != null) && (f006.length() > 12) ? Character.toString(f006.charAt(12)) : "";
	    
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		String f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toString(f007.charAt(1)) : "";
	    
		String f008 = underlayingMarc.getControlField("008");
	    String f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toString(f008.charAt(23)) : "";
	    String f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toString(f008.charAt(29)) : "";

		if(ldr06.matches("(?i)[acdpt]") && f008_23.matches("(?i)d")){
			return true;
		}
		if(ldr06.matches("(?i)[efk]") && f008_29.matches("(?i)d")){
			return true;
		}
		if(f006_00.matches("(?i)[acdpt]") && f006_06.matches("(?i)d")){
			return true;
		}
		if(f006_00.matches("(?i)[efk]") && f006_12.matches("(?i)d")){
			return true;
		}
		if(f007_00.matches("(?i)d") && f007_01.matches("(?i)b")){
			return true;
		}
		if(f007_00.matches("(?i)t") && f007_01.matches("(?i)b")){
			return true;
		}
		return false;
	}
	
	protected boolean isBraill(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
	    String f006 = underlayingMarc.getControlField("006");
		String f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toString(f006.charAt(6)) : "";
	    String f006_12 = (f006 != null) && (f006.length() > 12) ? Character.toString(f006.charAt(12)) : "";
	    
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		String f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toString(f007.charAt(1)) : "";
	    
		String f008 = underlayingMarc.getControlField("008");
	    String f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toString(f008.charAt(23)) : "";
	    String f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toString(f008.charAt(29)) : "";

	    String f245h = underlayingMarc.getField("245", 'h');		
		if(f245h == null) f245h = "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
	    
		if(ldr06.matches("(?i)[acdpt]") && f008_23.matches("(?i)f")){
			return true;
		}
		if(ldr06.matches("(?i)[acdpt]") && f006_06.matches("(?i)f")){
			return true;
		}
		if(f007_00.matches("(?i)f") && f007_01.matches("(?i)b") && f245h.matches("(?i).*hmatové\\spísmo.*")){
			return true;
		}
		if(ldr06.matches("(?i)[efk]")&& f008_29.matches("(?i)f")){
			return true;
		}
		if(ldr06.matches("(?i)[efk]") && f006_12.matches("(?i)f")){
			return true;
		}
		
		if(f007_00.matches("(?i)t") && f007_01.matches("(?i)c")){
			return true;
		}
		if(f336b.matches("(?i)tct|tcm|tci|tcf")) return true;
		
		return false;
	}
	
	protected boolean isElectronicSource(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
	    String f006 = underlayingMarc.getControlField("006");
	    String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		String f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toString(f006.charAt(6)) : "";
		
	    String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
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
		if(ldr06.matches("(?i)[acdijpt]") && f008_23.matches("(?i)[soq]")){
			return true;
		}
		if(f006_00.matches("(?i)[acdijpt]") && f006_06.matches("(?i)[soq]")){
			return true;
		}
		if(ldr06.matches("(?i)[efgkopr]") && f008_29.matches("(?i)[soq]")){
			return true;
		}
		
		if(f006_00.matches("(?i)[efgkopr]") && f006_06.matches("(?i)[soq]")){
			return true;
		}
		if(ldr06.matches("(?i)m") && f006_00.matches("(?i)m")){
			return true;
		}
		if(ldr06.matches("(?i)m") && f245h.matches("(?i).*multim[eé]dium.*") && f300a.matches("(?i).*cd-rom.*")) return true;
		if(f007_00.matches("(?i)c")) return true;
		if(f300a.matches("(?i).*disketa.*")) return true;
		if(f336b.matches("(?i)cod|cop")) return true;
		if(f338b.matches("(?i)cr|ck|cb|cd|ce|ca|cf|ch|cz")) return true;
		
		
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
		if(f300.matches("(?i).*kompaktn[ií]\\sdisk.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		if(f500.matches("(?i).*kompaktn[ií]\\sdisk.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		if(f300.matches("((?i).*zvukov[eéaá])\\sCD.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		if(f300a.matches(".*CD.*")) {
			if(!f300a.matches(".*CD-ROM.*")) return HarvestedRecordFormatEnum.AUDIO_CD;
		}
		if(f300.matches("(?i).*zvukov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*") && f300.matches("(?i).*(digital|12\\scm).*")) return HarvestedRecordFormatEnum.AUDIO_CD;

		// AUDIO_DVD
		if(ldr06.matches("(?i)[ij]") && f300a.matches("(?i).*dvd.*")) return HarvestedRecordFormatEnum.AUDIO_DVD;
		
		// AUDIO_LP
		if(f300.matches("(?i).*gramofonov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300.matches("(?i).*zvukov([aáeé]|ych|ých)\\sdes(ka|ky|ek).*") && f300.matches("(?i).*analog.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300a.matches(".*LP.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		if(f300a.matches(".*SP.*")) return HarvestedRecordFormatEnum.AUDIO_LP;
		
		// AUDIO_CASSETTE
		if(ldr06.matches("(?i)[ij]") && f007_00.matches("(?i)s")) return HarvestedRecordFormatEnum.AUDIO_CASSETTE;
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
		
		// Bluray
		if(ldr06.matches("(?i)g") && f300.matches("(?i).*blu.*ray.*")) return HarvestedRecordFormatEnum.VIDEO_BLURAY;
		
		// VHS
		if(f300.matches("(?i).*vhs.*")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		if(f007_00.matches("(?i)v") && f007_04.matches("(?i)b")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		if(f300a.matches("(?i).*videokazeta.*")) return HarvestedRecordFormatEnum.VIDEO_VHS;
		
		// DVD
		if(ldr06.matches("(?i)g") && f300a.matches("(?i).*dvd.*")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		if(f007_00.matches("(?i)v") && f007_04.matches("(?i)v")) return HarvestedRecordFormatEnum.VIDEO_DVD;
		
		// CD
		if(ldr06.matches("(?i)g") && f300a.matches("(?i).*cd.*")) return HarvestedRecordFormatEnum.VIDEO_CD;
		
		// others
		if(ldr06.matches("(?i)g")) return HarvestedRecordFormatEnum.VIDEO_OTHER;
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
	
	protected boolean isKit(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
	    String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
	    if(ldr06.matches("(?i)o")) return true;
	    if(f006_00.matches("(?i)o") && f007_00.matches("(?i)o")) return true;
		return false;
	}
	
	protected boolean isObject(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
		String f006 = underlayingMarc.getControlField("006");
	    String f006_00 = (f006 != null) && (f006.length() > 0) ? Character.toString(f006.charAt(0)) : "";
		
		String f008 = underlayingMarc.getControlField("008");
	    String f008_33 = (f008 != null) && (f008.length() > 33) ? Character.toString(f008.charAt(33)) : "";
	    		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
				
	    if(ldr06.matches("(?i)r")) return true;
	    if(f336b.matches("(?i)tcf|tdm|tdf")) return true;
	    if(f008_33.matches("(?i)d")) return true;
	    if(f006_00.matches("(?i)r")) return true;
		return false;
	}
	
	protected boolean isMixDocument(){
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord());
		
	    if(ldr06.matches("(?i)p")) return true;
		return false;
	}
	
	protected boolean isUnspecified(){
		String f007 = underlayingMarc.getControlField("007");
	    String f007_00 = (f007 != null) && (f007.length() > 0) ? Character.toString(f007.charAt(0)) : "";
		
		String f336b = underlayingMarc.getField("336", 'b');
		if(f336b == null) f336b = "";
		
		String f337b = underlayingMarc.getField("337", 'b');
		if(f337b == null) f337b = "";
				
		String f338b = underlayingMarc.getField("338", 'b');
		if(f338b == null) f338b = "";
		
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
		if(isMap()) hrf.add(HarvestedRecordFormatEnum.MAPS);
		if(isMusicalScores()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if(isVisualDocument()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if(isManuscript()) hrf.add(HarvestedRecordFormatEnum.MANUSCRIPTS);
		if(isMicroform()) hrf.add(HarvestedRecordFormatEnum.MICROFORMS);
		if(isLargePrint()) hrf.add(HarvestedRecordFormatEnum.LARGE_PRINTS);
		if(isBraill()) hrf.add(HarvestedRecordFormatEnum.BRAILL);
		if(isElectronicSource()) hrf.add(HarvestedRecordFormatEnum.ELECTRONIC_SOURCE);
		HarvestedRecordFormatEnum audio = getAudioFormat();
		if(audio != null) hrf.add(audio);
		HarvestedRecordFormatEnum video = getVideoDocument();
		if(video != null) hrf.add(video);
		if(isKit()) hrf.add(HarvestedRecordFormatEnum.OTHER_KIT);
		if(isObject()) hrf.add(HarvestedRecordFormatEnum.OTHER_OBJECT);
		if(isMixDocument()) hrf.add(HarvestedRecordFormatEnum.OTHER_MIX_DOCUMENT);
		if(isUnspecified()) hrf.add(HarvestedRecordFormatEnum.OTHER_UNSPECIFIED);		
		if(hrf.isEmpty()) hrf.add(HarvestedRecordFormatEnum.OTHER_UNSPECIFIED);
		
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
			String strValue = matcher.group(0).replaceAll("[\\ \\^]+", "");
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
		
        boolean f245Ind1 = false;
		for (String key: fields1xx){
			if(!underlayingMarc.getDataFields(key).isEmpty()){;
				for(DataField dataField: underlayingMarc.getDataFields("245")){
					if(dataField.getIndicator1() == 0) f245Ind1 = true;
				}
				break;
			}
		}
		if(!f245Ind1){
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


}
