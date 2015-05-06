package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.MetadataUtils;

@Component
public class MarcXmlDedupKeyParser implements DedupKeysParser {

	private final static String FORMAT = "marc21-xml";
	
	private final static int EFFECTIVE_TITLE_LENGTH = 255;
	
	@Autowired 
	private MetadataRecordFactory metadataFactory;

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat()));
		MetadataRecord metadata = metadataFactory.getMetadataRecord(record);

		record.setIsbns(metadata.getISBNs());
		List<Title> titles = new ArrayList<>();
		for (Title title: metadata.getTitle()) {
			title.setTitleStr(MetadataUtils.normalizeAndShorten(
						title.getTitleStr(),
						EFFECTIVE_TITLE_LENGTH));
			titles.add(title);
		}
		
		record.setIssns(metadata.getISSNs());
		record.setCnb(metadata.getCNBs());
		record.setTitles(titles);
		record.setPhysicalFormat(metadata.getFormat());
		record.setPublicationYear(metadata.getPublicationYear());

		return record;
	}
}
