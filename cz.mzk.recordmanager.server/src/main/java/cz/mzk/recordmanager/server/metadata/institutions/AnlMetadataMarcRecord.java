package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.view.ViewTypeEnum;

import java.util.Collections;
import java.util.List;

public class AnlMetadataMarcRecord extends MetadataMarcRecord {

	public AnlMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public List<ViewTypeEnum> getViewType() {
		return isIrelView() ? Collections.singletonList(ViewTypeEnum.IREL) : Collections.emptyList();
	}
}
