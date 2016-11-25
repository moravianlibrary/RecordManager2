package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;

public class Kram3NkpMetadataDublinCoreRecord extends KramDefaultMetadataDublinCoreRecord{

	protected static final Pattern HTTP_PATTERN = Pattern.compile("^http://kramerius.*");
	
	public Kram3NkpMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}
	
	@Override
	public List<String> getUrls() {
		List<String> result = new ArrayList<>();
		for (String item : dcRecord.getIdentifiers()) {
			if (HTTP_PATTERN.matcher(item).matches()) {
				result.add("unknown|"+item+"|");
			}
		}
		result.addAll(super.getUrls());
		return result;
	}

}
