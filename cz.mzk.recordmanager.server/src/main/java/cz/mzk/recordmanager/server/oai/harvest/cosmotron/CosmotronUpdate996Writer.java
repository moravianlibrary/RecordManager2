package cz.mzk.recordmanager.server.oai.harvest.cosmotron;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.hibernate.SessionFactory;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class CosmotronUpdate996Writer implements ItemWriter<HarvestedRecordUniqueId> {

	@Autowired
	protected HarvestedRecordDAO hrDao;

	@Autowired
	protected Cosmotron996DAO cosmotronDao;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> uniqueIds) throws Exception {
		for (HarvestedRecordUniqueId uniqueId : uniqueIds) {
			move996ToParentRecord(uniqueId);
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private void move996ToParentRecord(HarvestedRecordUniqueId parentUniqueId) {
		List<Cosmotron996> childRecs = cosmotronDao.findByParentId(parentUniqueId);
		HarvestedRecord parentRec = hrDao.get(parentUniqueId);
		// ignore deleted HarvestedRecord
		if (parentRec.getDeleted() != null) return;
		updateMarc(parentRec, childRecs);
		parentRec.setUpdated(new Date());
		hrDao.persist(parentRec);
	}

	private void updateMarc(HarvestedRecord parentRec, List<Cosmotron996> childRecs) {
		Record record = marcXmlParser.parseUnderlyingRecord(parentRec.getRawRecord());
		Record newRecord = new RecordImpl();
		newRecord.setLeader(record.getLeader());
		for (ControlField cf : record.getControlFields()) {
			newRecord.addVariableField(cf);
		}
		for (DataField df : record.getDataFields()) {
			// remove old fields 996
			if (!df.getTag().equals("996")) {
				newRecord.addVariableField(df);
			}
		}
		for (Cosmotron996 new996 : childRecs) {
			if (new996.getDeleted() != null) continue;
			MarcRecord marcRecord996 = parseMarcRecord(new996.getRawRecord());
			for (DataField df : get996(marcRecord996)) {
				newRecord.addVariableField(df);
			}
		}
		parentRec.setRawRecord(new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8));
	}

	private List<DataField> get996(MarcRecord marcRecord) {
		return marcRecord.getDataFields("996");
	}

	private MarcRecord parseMarcRecord(byte[] rawRecord) {
		Record record = marcXmlParser.parseUnderlyingRecord(rawRecord);
		return new MarcRecordImpl(record);
	}
}
