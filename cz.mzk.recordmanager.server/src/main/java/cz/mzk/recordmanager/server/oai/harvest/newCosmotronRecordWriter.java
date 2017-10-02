package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.util.CosmotronUtils;

public class newCosmotronRecordWriter extends HarvestedRecordWriter implements ItemWriter<List<HarvestedRecord>> {

	@Autowired
	protected Cosmotron996DAO cosmotronDao;

	@Autowired
	protected MarcXmlParser marcXmlParser;

	protected Long configurationId;

	public newCosmotronRecordWriter(Long configId) {
		this.configurationId = configId;
	}

	@Override
	public void write(List<? extends List<HarvestedRecord>> records) throws Exception {
		for (List<HarvestedRecord> list : records) {
			for (HarvestedRecord record : list) {
				processAndSave(record);	
			}
		}
	}

	protected void processAndSave(HarvestedRecord hr) throws Exception {
		if (hr.getId() == null) {
			InputStream is = new ByteArrayInputStream(hr.getRawRecord());
			MarcRecord mr = marcXmlParser.parseRecord(is);
			String parentRecordId = CosmotronUtils.get77308w(mr);
			if (parentRecordId == null) {
				super.writeRecord(hr); // new harvested_record
			}			
			else {
				String recordId = hr.getUniqueId().getRecordId();
				Cosmotron996 cr = cosmotronDao.findByIdAndHarvestConfiguration(recordId, configurationId);
				if (cr == null) { 
					cr = new Cosmotron996(recordId, configurationId);
					cr.setHarvested(new Date());
				}
				cr.setParentRecordId(parentRecordId);
				cr.setUpdated(new Date());
				if (hr.getDeleted() != null) {
					cr.setDeleted(new Date());
					cr.setRawRecord(new byte[0]);
				}
				else {
					cr.setDeleted(null);
					cr.setRawRecord(hr.getRawRecord());
				}
				cosmotronDao.persist(cr);
			}
		}
		else super.writeRecord(hr); // 
	}

}
