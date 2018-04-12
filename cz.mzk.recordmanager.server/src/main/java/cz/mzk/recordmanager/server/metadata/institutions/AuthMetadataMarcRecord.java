package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.util.Constants;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class AuthMetadataMarcRecord extends MetadataMarcRecord{

	public AuthMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		if(underlayingMarc.getDataFields("100").isEmpty()) return false;

		for(DataField df: underlayingMarc.getDataFields("100")){
			if(df.getSubfield('t') != null) return false;
		}

		return true;
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.OTHER_PERSON);

	}

	@Override
	public String getAuthorityId(){
		for(DataField df: underlayingMarc.getDataFields("100")){
			Subfield sf = df.getSubfield('7');
			if(sf != null) return sf.getData();
		}
		return null;
	}

	@Override
	public List<String> getUrls() {
		List<String> results = super.getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE);
		for (String link : underlayingMarc.getFields("998", 'a')) {
			results.add(generateUrl(Constants.DOCUMENT_AVAILABILITY_ONLINE, link, ""));
		}
		return results;
	}
}
