package cz.mzk.recordmanager.server.miscellaneous.caslin.filter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.index.SolrRecordProcessor;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.CaslinFilter;

public class FilterCaslinRecordsWriter implements ItemWriter<HarvestedRecordUniqueId> {

	private static Logger logger = LoggerFactory.getLogger(SolrRecordProcessor.class);

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private MetadataRecordFactory mrFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private CaslinFilter caslinFilter;

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> items)
			throws Exception {

		for (HarvestedRecordUniqueId uniqueId : items) {
			try {
				HarvestedRecord hr = hrDao.get(uniqueId);

				if (hr == null || hr.getRawRecord().length == 0) continue;
				MarcRecord marc = marcXmlParser.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				Record record = marcXmlParser.parseUnderlyingRecord(new ByteArrayInputStream(hr.getRawRecord()));
				Boolean updated = false;
				Record newRecord = new RecordImpl();
				MarcFactory marcFactory = new MarcFactoryImpl();
				newRecord.setLeader(record.getLeader());
				for (ControlField cf : record.getControlFields()) {
					newRecord.addVariableField(cf);
				}

				Map<String, List<DataField>> dfMap = marc.getAllFields();
				for (String tag : new TreeSet<>(dfMap.keySet())) {
					for (DataField df : dfMap.get(tag)) {
						// add $q0 when sigla is in db
						if (df.getTag().equals("996")) {
							if (caslinFilter.filter(df.getSubfield('e').getData())
									&& (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0"))) {
								df.addSubfield(marcFactory.newSubfield('q', "0"));
								updated = true;
							}
						}
						newRecord.addVariableField(df);
					}
				}
				hr.setRawRecord(new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8));
				if (hr.getDeleted() == null && !mrFactory.getMetadataRecord(hr, marc).matchFilter()) {
					hr.setDeleted(new Date());
					updated = true;
				}
				if (updated) {
					hr.setUpdated(new Date());
					hrDao.persist(hr);
				}
			} catch (Exception ex) {
				logger.error(String.format("Exception thrown when filtering harvested_record with id=%s", uniqueId), ex);
			}
		}
	}
}
