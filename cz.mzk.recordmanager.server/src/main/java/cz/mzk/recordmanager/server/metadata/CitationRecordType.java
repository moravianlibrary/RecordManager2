package cz.mzk.recordmanager.server.metadata;

public enum CitationRecordType {
	ACADEMIC_WORK("DS"),
	ELECTRONIC_ACADEMIC_WORK("eDS"),
	MANUSCRIPTS("ND"),
	ELECTRONIC_BOOK("eBK"),
	BOOK("BK"),
	BOOK_PART("PB"),
	ELECTRONIC_BOOK_PART("ePB"),
	ELECTRONIC_PERIODICAL("eSE"),
	PERIODICAL("SE"),
	ELECTRONIC_CONTRIBUTION_PROCEEDINGS("ePC"),
	CONTRIBUTION_PROCEEDINGS("PC"),
	ELECTRONIC_ARTICLE("eAR"),
	ARTICLE("AR"),
	MAPS("MP"),
	OTHERS("MX"),
	NORMS("NO"),
	LAW("LE"),
	PATENT("PA"),
	PROCEEDINGS("PR"),
	ELECTRONIC_PROCEEDINGS("ePR"),
	ERROR("error");
	
	private String format;
	
	CitationRecordType(String format){
		this.format = format;
	}
	
	public String getCitationType(){
		return format;
	}
}
