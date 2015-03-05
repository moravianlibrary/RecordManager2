package cz.mzk.recordmanager.server.scripting.function;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class PublishDateMarcFunctions implements MarcRecordFunctions {

	public String getPublishDate(MarcRecord record) {
		return null;
	}

	public String getPublishDateForSorting(MarcRecord record) {
		return null;
	}

}
