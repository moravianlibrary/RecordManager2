package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class MarcSolrRecordMapper implements SolrRecordMapper {
	
	private static final String ID_FIELD = "id";
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		HarvestedRecord record = records.get(0);
		SolrInputDocument document = parse(record);
		document.addField(ID_FIELD, dedupRecord.getId());
		return document;
	}
	
	protected SolrInputDocument parse(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		SolrInputDocument document = new SolrInputDocument();
		MarcRecord rec = marcXmlParser.parseRecord(is);
		return document;
	}

}
