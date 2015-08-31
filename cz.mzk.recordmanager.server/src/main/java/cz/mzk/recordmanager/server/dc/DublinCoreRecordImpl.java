package cz.mzk.recordmanager.server.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DublinCoreRecordImpl implements DublinCoreRecord {

	/* <MJ.> - only variables needed for Kramerius dedup keys are implemented */
	private List<String> contributor;
	private List<String> coverage;
	private List<String> creator;
	private List<String> date;
	private List<String> description;
	private List<String> format;
	private List<String> identifier;
	private List<String> language;
	private List<String> publisher;
	private List<String> relation;
	private List<String> rights;
	private List<String> source;
	private List<String> subject;
	private List<String> title;
	private List<String> type;
	
	private byte[] rawRecord = new byte[0];

	public DublinCoreRecordImpl() {
		creator = new ArrayList<>();
		date = new ArrayList<>();
		format = new ArrayList<>();
		identifier = new ArrayList<>();
		title = new ArrayList<>();
		type = new ArrayList<>();

	}

	@Override
	public String getFirstCreator() {
		if (creator.isEmpty()) {
			return this.creator.get(0);
		}
		return null;
	}

	@Override
	public String getFirstDate() {
		if (!date.isEmpty()) {
			return this.date.get(0);
		}
		return null;
	}

	@Override
	public String getFirstFormat() {
		if (!format.isEmpty()) {
			return this.format.get(0);
		}
		return null;
	}

	@Override
	public String getFirstIdentifier() {
		if (!identifier.isEmpty()) {
			return this.identifier.get(0);
		}
		return null;
	}

	@Override
	public String getFirstTitle() {

		if (!title.isEmpty()) {
			return this.title.get(0);
		}
		return null;
	}

	@Override
	public String getFirstType() {
		if (!type.isEmpty()) {
			return this.type.get(0);
		}
		return null;
	}

	@Override
	public void addCreator(String c) {
		this.creator.add(c);
	}

	@Override
	public void addDate(String s) {
		this.date.add(s);

	}

	@Override
	public void addFormat(String s) {
		this.format.add(s);
	}

	@Override
	public void addIdentifier(String id) {
		this.identifier.add(id);

	}

	@Override
	public void addTitle(String t) {
		this.title.add(t);
	}

	@Override
	public void addType(String s) {
		this.type.add(s);

	}

	@Override
	public List<String> getDates() {
		if (this.date != null) {
			return this.date;
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getIdentifiers() {
		if (this.identifier != null) {
			return this.identifier;
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getTitles() {
		if (this.title != null) {
			return this.title;
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getTypes() {
		if (this.type != null) {
			return this.type;
		}
		return Collections.emptyList();
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
