package cz.mzk.recordmanager.server.oai.harvest.cosmotron;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.harvest.HarvestedRecordWriter;
import cz.mzk.recordmanager.server.util.CosmotronUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CosmotronRecordWriter extends HarvestedRecordWriter implements ItemWriter<List<HarvestedRecord>> {

	@Autowired
	protected Cosmotron996DAO cosmotronDao;

	@Autowired
	protected MarcXmlParser marcXmlParser;

	protected Long configurationId;

	public CosmotronRecordWriter(Long configId) {
		this.configurationId = configId;
	}

	@Override
	public void write(List<? extends List<HarvestedRecord>> records) throws Exception {
		for (List<HarvestedRecord> list : records) {
			for (HarvestedRecord record : list) {
				processAndSave(record);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private void processAndSave(HarvestedRecord hr) {
		if (hr.getId() == null) {
			String recordId = hr.getUniqueId().getRecordId();
			// save deleted record
			if (hr.getDeleted() != null) {
				Cosmotron996 deleted996;
				if ((deleted996 = cosmotronDao.findByIdAndHarvestConfiguration(recordId, configurationId)) == null) {
					// new deleted record
					super.writeRecord(hr);
				} else {
					// delete existing 996 record
					deleted996.setDeleted(new Date());
					deleted996.setUpdated(new Date());
					deleted996.setRawRecord(new byte[0]);
					deleted996.setLastHarvest(new Date());
					cosmotronDao.persist(deleted996);
				}
				return;
			}
			// process not deleted record
			InputStream is = new ByteArrayInputStream(hr.getRawRecord());
			MarcRecord mr = marcXmlParser.parseRecord(is);
			String parentRecordId = CosmotronUtils.getParentId(mr);
			if (parentRecordId == null) {
				super.writeRecord(hr); // new harvested_record
			} else {
				// new or updated 996 record
				Cosmotron996 cr = cosmotronDao.findByIdAndHarvestConfiguration(recordId, configurationId);
				if (cr == null) {
					// create new record
					cr = new Cosmotron996(recordId, configurationId);
					cr.setHarvested(new Date());
				} else if (hr.getRawRecord() == null || hr.getRawRecord().length == 0
						|| Arrays.equals(cr.getRawRecord(), hr.getRawRecord())) {
					cr.setLastHarvest(new Date());
					cosmotronDao.persist(cr);
					return;
				}
				cr.setParentRecordId(parentRecordId);
				cr.setUpdated(new Date());
				cr.setDeleted(null);
				cr.setLastHarvest(new Date());
				cr.setRawRecord(hr.getRawRecord());
				if (!CosmotronUtils.existsFields996(mr)) {
					// record with parent id and without fields 996
					// save and set deleted value
					cr.setDeleted(new Date());
					cr.setParentRecordId(null);
				}
				cosmotronDao.persist(cr);
			}
		} else super.writeRecord(hr); // process existing harvested record
	}

}
