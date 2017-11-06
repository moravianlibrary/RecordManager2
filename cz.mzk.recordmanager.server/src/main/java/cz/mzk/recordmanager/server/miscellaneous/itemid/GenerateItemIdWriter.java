package cz.mzk.recordmanager.server.miscellaneous.itemid;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.intercepting.DefaultMarcInterceptor;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;

public class GenerateItemIdWriter implements ItemWriter<HarvestedRecordUniqueId>{

	private static Logger logger = LoggerFactory.getLogger(GenerateItemIdWriter.class);

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private MarcXmlParser marcXmlParser;

	private ProgressLogger progress = new ProgressLogger(logger, 10000);

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> items)
			throws Exception {
		
		for(HarvestedRecordUniqueId uniqueId: items){
			try{
				progress.incrementAndLogProgress();
				HarvestedRecord hr = hrDao.get(uniqueId);
				if (hr == null || hr.getRawRecord() == null
						|| hr.getRawRecord().length == 0
						|| !hr.getFormat().equals("marc21-xml")
						|| hr.getHarvestedFrom().getItemId() == null) {
					continue;
				}

				Record record = marcXmlParser.parseUnderlyingRecord(new ByteArrayInputStream(hr.getRawRecord()));
				hr.setRawRecord(new DefaultMarcInterceptor(record, hr.getHarvestedFrom(), uniqueId.getRecordId()).intercept());
			}
			catch(Exception ex){
				logger.error(String.format("Exception thrown in harvested_record with id=%s", uniqueId), ex);
			}
		}	
	}
}
