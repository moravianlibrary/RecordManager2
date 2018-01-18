package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.ViewType;

import java.util.Collections;
import java.util.List;

public class IirMetadataMarcRecord extends MetadataMarcRecord {

	public IirMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public List<ViewType> getViewType() {
		return isIrelView() ? Collections.emptyList() : Collections.singletonList(ViewType.IREL);
	}
}
