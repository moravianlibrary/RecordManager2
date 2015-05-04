package cz.mzk.recordmanager.server.dc;

import java.util.List;

public interface DublinCoreRecord {

	/* -- get --- */

	public String getFirstCreator();

	/* gets first creator */

	public String getFirstDate();

	/* gets first date */

	public String getFirstFormat();

	/* gets first format */

	public String getFirstIdentifier();

	/* gets first identifier */

	public String getFirstTitle();

	/* gets first title */

	public String getFirstType();

	/* gets first title */

	/* --- add --- */

	public void addTitle(String s);

	/* adds title to title list */

	public void addCreator(String s);

	/* adds creator to creator list */

	public void addDate(String s);

	/* adds date to date list */

	public void addFormat(String s);

	/* adds format to format list */

	public void addIdentifier(String s);

	/* adds identifier to identifier list */

	public void addType(String s);

	/* adds type to type list */

	/* --- get List --- */

	public List<String> getDates();

	/* gets list of all identifiers */

	public List<String> getIdentifiers();

	/* gets list of all identifiers */

	public List<String> getTitles();

	/* gets list of all titles */

	public List<String> getTypes();
	/* gets list of all types */

}
