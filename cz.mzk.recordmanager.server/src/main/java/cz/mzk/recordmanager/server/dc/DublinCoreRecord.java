package cz.mzk.recordmanager.server.dc;

import cz.mzk.recordmanager.server.export.IOFormat;

import java.util.List;

public interface DublinCoreRecord {

/*  works with set of 15 Dublin Core elements:
	 contributor 
	 coverage 
	 creator 
	 date x
	 description  
	 format 
	 identifier 
	 language 
	 publisher 
	 relation 
	 rights 
	 source 
	 subject 
	 title
	 type
	 
	ese
	 url
	 physical
	 content
	 title alternative
*/	
	
	/* --- get List --- */

	/* gets list of all contributors */
	List<String> getContributors();
	
	/* gets list of all coverages */
	List<String> getCoverages();
	
	/* gets list of all creators */
	List<String> getCreators();
	
	/* gets list of all identifiers */
	List<String> getDates();
	
	/* gets list of all descriptions */
	List<String> getDescriptions();
	
	/* gets list of all formats */
	List<String> getFormats();

	/* gets list of all identifiers */
	List<String> getIdentifiers();
	
	/* gets list of all languages */
	List<String> getLanguages();

	/* gets list of all publishers */
	List<String> getPublishers();
	
	/* gets list of all relations */
	List<String> getRelations();
	
	/* gets list of all rights */
	List<String> getRights();
	
	/* gets list of all sources */
	List<String> getSources();
	
	/* gets list of all subjects */
	List<String> getSubjects();
	
	/* gets list of all titles */
	List<String> getTitles();

	/* gets list of all types */
	List<String> getTypes();
	
	/* gets list of all urls */
	List<String> getUrls();
	
	/* gets list of all physicals */
	List<String> getPhysicals();
	
	/* gets list of all contents */
	List<String> getContents();
	
	/* gets list of all title alternatives */
	List<String> getTitleAlts();
	
	/* --- add --- */

	/* adds to contributor list*/
	void addContributor(String s);
	
	/* adds to coverage list*/
	void addCoverage(String s);
	
	/* adds to creator list */
	void addCreator(String s);
	
	/* adds to date list */
	void addDate(String s);
	
	/* adds to description list */
	void addDescription(String s);
	
	/* adds to format list */
	void addFormat(String s);
	
	/* adds to title list */
	void addTitle(String s);

	/* adds to identifier list */
	void addIdentifier(String s);

	/* adds to languages list */
	void addLanguage(String s);
	
	/* adds to publisher list */
	void addPublisher(String s);
	
	/* adds to relations list */
	void addRelation(String s);
	
	/* adds to rights list */
	void addRights(String s);
	
	/* adds to sources list */
	void addSource(String s);
	
	/* adds to subjects list */
	void addSubjects(String s);
	
	/* adds to type list */
	void addType(String s);
	
	/* adds to url list */
	void addUrls(String s);
	
	/* adds to physical list */
	void addPhysical(String s);
	
	/* adds to content list */
	void addContent(String s);
	
	/* adds to title alternatives list */
	void addTitleAlt(String s);
	
	/* -- get first --- */

	/* gets first creator */
	String getFirstCreator();

	/* gets first date */
	String getFirstDate();

	/* gets first format */
	String getFirstFormat();

	/* gets first identifier */
	String getFirstIdentifier();

	/* gets first title */
	String getFirstTitle();

	/* gets first title */
	String getFirstType();

	/* export record in given format */
	String export(IOFormat iOFormat);

	byte[] getRawRecord();
	
	void setRawRecord(byte[] rawRecord);

}
