package cz.mzk.recordmanager.server.dedup;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordFormatDAO;
import cz.mzk.recordmanager.server.util.MetadataUtils;

@Component
public class DublinCoreDedupKeyParser implements DedupKeysParser {

	private final static String FORMAT = "dublinCore";
	
	private final static int EFFECTIVE_TITLE_LENGTH = 255;
	
	@Autowired 
	private MetadataRecordFactory metadataFactory;
	
	@Autowired 
	private HarvestedRecordFormatDAO harvestedRecordFormatDAO;
	
	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat()));
		MetadataRecord metadata = metadataFactory.getMetadataRecord(record);

		record.setIsbns(metadata.getISBNs());
		List<Title> existingTitles = record.getTitles();
		for (Title title: metadata.getTitle()) {
			title.setTitleStr(MetadataUtils.normalizeAndShorten(
						title.getTitleStr(),
						EFFECTIVE_TITLE_LENGTH));
			if (!existingTitles.contains(title)) {
				existingTitles.add(title);
			}
		}
		record.setTitles(existingTitles);
		
		record.setIssns(metadata.getISSNs());
		record.setCnb(metadata.getCNBs());
		if(record.getHarvestedFrom() != null) record.setWeight(metadata.getWeight(record.getHarvestedFrom().getBaseWeight()));
		record.setPublicationYear(metadata.getPublicationYear());
		List<HarvestedRecordFormatEnum> formatEnums = metadata.getDetectedFormatList();
		record.setPhysicalFormats(harvestedRecordFormatDAO.getFormatsFromEnums(formatEnums));
		record.setAuthorAuthKey(metadata.getAuthorAuthKey());
		record.setAuthorString(MetadataUtils.normalize(metadata.getAuthorString()));
		record.setScale(metadata.getScale());
		record.setUuid(metadata.getUUId());
		record.setIssnSeries(MetadataUtils.normalize(metadata.getISSNSeries()));
		record.setIssnSeriesOrder(MetadataUtils.normalize(metadata.getISSNSeriesOrder()));
		return record;
	}
}
