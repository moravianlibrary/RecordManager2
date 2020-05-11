package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;

import java.util.List;

public class KramKnavMetadataDublinCoreRecord extends
		KramDefaultMetadataDublinCoreRecord {

	public KramKnavMetadataDublinCoreRecord(DublinCoreRecord dcRecord, HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<Title> getTitle() {
		List<Title> titles = super.getTitle();
		if (titles.size() > 1) {
			StringBuilder mergedTitle = new StringBuilder();
			for (Title title : titles) {
				mergedTitle.append(title.getTitleStr());
			}
			titles.add(Title.create(mergedTitle.toString(), titles.size() + 1));
		}
		return titles;
	}

}
