package cz.mzk.recordmanager.server.dedup;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.dc.InvalidDcException;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordFormatDAO;

@Component
public class DublinCoreDedupKeyParser extends HashingDedupKeyParser {

	private final static String FORMAT = "dublinCore";
	private final static String FORMAT_ESE = "ese";
	
	@Autowired 
	private MetadataRecordFactory metadataFactory;
	
	@Autowired 
	private HarvestedRecordFormatDAO harvestedRecordFormatDAO;
	
	@Override
	public List<String> getSupportedFormats() {
		return Arrays.asList(FORMAT, FORMAT_ESE);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat())
				|| FORMAT_ESE.equals(record.getFormat()));
		try {		
			MetadataRecord metadata = metadataFactory.getMetadataRecord(record);
			return parse(record, metadata);
		} catch (InvalidDcException idce) {
			throw new DedupKeyParserException(idce.getMessage(), idce);
		}
	}

}
