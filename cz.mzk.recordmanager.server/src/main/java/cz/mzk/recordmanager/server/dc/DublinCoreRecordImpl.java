package cz.mzk.recordmanager.server.dc;

import java.util.ArrayList;
import java.util.List;

public class DublinCoreRecordImpl implements DublinCoreRecord {

	private List<String> contributor = new ArrayList<>();
	private List<String> coverage = new ArrayList<>();
	private List<String> creator = new ArrayList<>();
	private List<String> date = new ArrayList<>();
	private List<String> description = new ArrayList<>();
	private List<String> format = new ArrayList<>();
	private List<String> identifier = new ArrayList<>();
	private List<String> language = new ArrayList<>();
	private List<String> publisher = new ArrayList<>();
	private List<String> relation = new ArrayList<>();
	private List<String> rights = new ArrayList<>();
	private List<String> source = new ArrayList<>();
	private List<String> subject = new ArrayList<>();
	private List<String> title = new ArrayList<>();
	private List<String> type = new ArrayList<>();
	
	private byte[] rawRecord = new byte[0];

	
	/* GET LIST */

	@Override
	public List<String> getCoverages() {
		if (this.coverage != null) {
			return this.coverage;
		}
		return new ArrayList<String>();
	}

	
	@Override
	public List<String> getContributors() {
		if (this.contributor != null) {
			return this.contributor;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getCreators() {
		if (this.creator != null) {
			return this.creator;
		}
		return new ArrayList<String>();
	}

	
	@Override
	public List<String> getDates() {
		if (this.date != null) {
			return this.date;
		}
		return new ArrayList<String>();
	}

	@Override
	public List<String> getDescriptions() {
		if (this.description != null) {
			return this.description;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getFormats() {
		if (this.format != null) {
			return this.format;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getIdentifiers() {
		if (this.identifier != null) {
			return this.identifier;
		}
		return new ArrayList<String>();
	}

	@Override
	public List<String> getLanguages() {
		if (this.language != null) {
			return this.language;
		}
		return new ArrayList<String>();
	}

	
	@Override
	public List<String> getPublishers() {
		if (this.publisher != null) {
			return this.publisher;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getRelations() {
		if (this.relation != null) {
			return this.relation;
		}
		return new ArrayList<String>();
	}
	
	
	@Override
	public List<String> getRights() {
		if (this.rights != null) {
			return this.rights;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getSources() {
		if (this.source != null) {
			return this.source;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getSubjects() {
		if (this.subject != null) {
			return this.subject;
		}
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getTitles() {
		if (this.title != null) {
			return this.title;
		}
		return new ArrayList<String>();
	}

	@Override
	public List<String> getTypes() {
		if (this.type != null) {
			return this.type;
		}
		return new ArrayList<String>();
	}

	
    /* ADD */

	@Override
	public void addContributor(String s) {
		this.contributor = getContributors();
		contributor.add(s);
	}
	
	@Override
	public void addCoverage(String s) {
		this.coverage = getCoverages();
		coverage.add(s);
	}
	
	@Override
	public void addCreator(String c) {
		this.creator = getCreators();
		creator.add(c);
	}

	@Override
	public void addDate(String s) {
		this.date = getDates();
		date.add(s);
	}

	@Override
	public void addDescription(String s) {
		this.description = getDescriptions();
		description.add(s);
	}
	
	@Override
	public void addFormat(String s) {
		this.format = getFormats();
		format.add(s);
	}

	@Override
	public void addLanguage(String s) {
		this.language = getLanguages();
		language.add(s);
	}
	
	@Override
	public void addIdentifier(String id) {
		this.identifier = getIdentifiers();
		identifier.add(id);
	}

	@Override
	public void addPublisher(String s) {
		this.publisher = getPublishers();
		publisher.add(s);
	}
	
	@Override
	public void addRelation(String s) {
		this.relation = getRelations();
		relation.add(s);
		
	}
	
	@Override
	public void addRights(String s) {
		this.rights = getRights();
		rights.add(s);
	}
	
	@Override
	public void addSource(String s) {
		this.source = getSources();
		source.add(s);
	}
	
	@Override
	public void addSubjects(String s) {
		this.subject = getSubjects();
		subject.add(s);
	}
	
	@Override
	public void addTitle(String t) {
		this.title = getTitles();
		title.add(t);
	}

	@Override
	public void addType(String s) {
		this.type = getTypes();
		type.add(s);
	}

	/* GET FIRST */
	
	@Override
	public String getFirstCreator() {
		if (!getCreators().isEmpty()) {
			return this.creator.get(0);
		}
		return null;
	}

	@Override
	public String getFirstDate() {
		if (!getDates().isEmpty()) {
			return this.date.get(0);
		}
		return null;
	}

	@Override
	public String getFirstFormat() {
		if (!getFormats().isEmpty()) {
			return this.format.get(0);
		}
		return null;
	}

	@Override
	public String getFirstIdentifier() {
		if (!getIdentifiers().isEmpty()) {
			return this.identifier.get(0);
		}
		return null;
	}

	@Override
	public String getFirstTitle() {

		if (!getTitles().isEmpty()) {
			return this.title.get(0);
		}
		return null;
	}

	@Override
	public String getFirstType() {
		if (!getTypes().isEmpty()) {
			return this.type.get(0);
		}
		return null;
	}

	@Override
	public byte[] getRawRecord() {
		return rawRecord;
	}

	@Override
	public void setRawRecord(byte[] rawRecord) {
		this.rawRecord = rawRecord;
		
	}

}
