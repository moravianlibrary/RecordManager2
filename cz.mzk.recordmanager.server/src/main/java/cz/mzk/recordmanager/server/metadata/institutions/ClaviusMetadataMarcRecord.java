package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClaviusMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern ID_START_LEADER = Pattern.compile("^(?:AV|ZK)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ID_START_OTHERS = Pattern.compile("^(?:SH)", Pattern.CASE_INSENSITIVE);

	public ClaviusMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	protected char getLeaderChar(char c) {
		return ID_START_LEADER.matcher(underlayingMarc.getControlField("001")).find() ? ' ' : super.getLeaderChar(c);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		if (ID_START_OTHERS.matcher(underlayingMarc.getControlField("001")).find()) {
			List<HarvestedRecordFormatEnum> result = new ArrayList<>();
			result.add(HarvestedRecordFormatEnum.OTHER_OTHER);
			return result;
		}
		return super.getDetectedFormatList();
	}
}
