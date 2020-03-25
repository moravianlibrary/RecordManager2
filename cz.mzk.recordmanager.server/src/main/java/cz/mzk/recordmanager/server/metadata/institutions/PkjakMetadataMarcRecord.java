package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.regex.Pattern;

public class PkjakMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern WEIGHT_PATTERN = Pattern.compile("S|SUK");
	private static final int SUK_LIBRARY_REDUCE_WEIGHT = 3;

	public PkjakMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public Long getWeight(Long baseWeight) {
		for (String sf996l : underlayingMarc.getFields("996", 'l')) {
			if (WEIGHT_PATTERN.matcher(sf996l).matches()) {
				baseWeight -= SUK_LIBRARY_REDUCE_WEIGHT;
				break;
			}
		}
		return super.getWeight(baseWeight);
	}
}
