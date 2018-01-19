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
	public List<String> getContributors();
	
	/* gets list of all coverages */
	public List<String> getCoverages();
	
	/* gets list of all creators */
	public List<String> getCreators();
	
	/* gets list of all identifiers */
	public List<String> getDates();
	
	/* gets list of all descriptions */
	public List<String> getDescriptions();
	
	/* gets list of all formats */
	public List<String> getFormats();

	/* gets list of all identifiers */
	public List<String> getIdentifiers();
	
	/* gets list of all languages */
	public List<String> getLanguages();

	/* gets list of all publishers */
	public List<String> getPublishers();	
	
	/* gets list of all relations */
	public List<String> getRelations();
	
	/* gets list of all rights */
	public List<String> getRights();
	
	/* gets list of all sources */
	public List<String> getSources();
	
	/* gets list of all subjects */
	public List<String> getSubjects();
	
	/* gets list of all titles */
	public List<String> getTitles();

	/* gets list of all types */
	public List<String> getTypes();
	
	/* gets list of all urls */
	public List<String> getUrls();
	
	/* gets list of all physicals */
	public List<String> getPhysicals();
	
	/* gets list of all contents */
	public List<String> getContents();
	
	/* gets list of all title alternatives */
	public List<String> getTitleAlts();
	
	/* --- add --- */

	/* adds to contributor list*/
	public void addContributor(String s);
	
	/* adds to coverage list*/
	public void addCoverage(String s);
	
	/* adds to creator list */
	public void addCreator(String s);
	
	/* adds to date list */
	public void addDate(String s);
	
	/* adds to description list */
	public void addDescription(String s);
	
	/* adds to format list */
	public void addFormat(String s);
	
	/* adds to title list */
	public void addTitle(String s);

	/* adds to identifier list */
	public void addIdentifier(String s);

	/* adds to languages list */
	public void addLanguage(String s);
	
	/* adds to publisher list */
	public void addPublisher(String s);
	
	/* adds to relations list */
	public void addRelation(String s);
	
	/* adds to rights list */
	public void addRights(String s);
	
	/* adds to sources list */
	public void addSource(String s);
	
	/* adds to subjects list */
	public void addSubjects(String s);
	
	/* adds to type list */
	public void addType(String s);
	
	/* adds to url list */
	public void addUrls(String s);
	
	/* adds to physical list */
	public void addPhysical(String s);
	
	/* adds to content list */
	public void addContent(String s);
	
	/* adds to title alternatives list */
	public void addTitleAlt(String s);
	
	/* -- get first --- */

	/* gets first creator */
	public String getFirstCreator();

	/* gets first date */
	public String getFirstDate();

	/* gets first format */
	public String getFirstFormat();

	/* gets first identifier */
	public String getFirstIdentifier();

	/* gets first title */
	public String getFirstTitle();

	/* gets first title */
	public String getFirstType();

	/* export record in given format */
	String export(IOFormat iOFormat);

	public byte[] getRawRecord();
	
	public void setRawRecord(byte[] rawRecord);

}
