package cz.mzk.recordmanager.server.metadata;

public enum CitationRecordType {
	ACADEMIC_WORK("DS"),
	MANUSCRIPTS("ND"), 
	ELECTRONIC_BOOK("eBK"),
	BOOK("BK"), 
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
	ERROR("error");
	
	private String format;
	
	CitationRecordType(String format){
		this.format = format;
	}
	
	public String getCitationType(){
		return format;
	}
}
