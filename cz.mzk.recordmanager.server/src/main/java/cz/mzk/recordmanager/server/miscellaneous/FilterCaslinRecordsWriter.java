package cz.mzk.recordmanager.server.miscellaneous;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
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
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.CaslinFilter;

public class FilterCaslinRecordsWriter implements ItemWriter<HarvestedRecordUniqueId>{

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
		
		for(HarvestedRecordUniqueId uniqueId: items){
			try{
				HarvestedRecord hr = hrDao.get(uniqueId);
				
				if(hr == null || hr.getRawRecord().length == 0) continue;
				MarcRecord marc = marcXmlParser.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				Record record = marcXmlParser.parseUnderlyingRecord(new ByteArrayInputStream(hr.getRawRecord()));
				List<DataField> old996 = marc.getDataFields("996");
				List<DataField> new996 = caslinFilter.filter(old996);
				Boolean updated = false;
				if(old996.size() != new996.size()){
					Record newRecord = new RecordImpl();
					
					newRecord.setLeader(record.getLeader());
					for (ControlField cf: record.getControlFields()) {
						newRecord.addVariableField(cf);
					}
					
					Map<String, List<DataField>> dfMap = marc.getAllFields();
					for (String tag: new TreeSet<String>(dfMap.keySet())) {
						for (DataField df: dfMap.get(tag)) {
							if(!df.getTag().equals("996")) {
								newRecord.addVariableField(df);
							}
						}
					}
					for(DataField df: new996){
						newRecord.addVariableField(df);
					}
					hr.setRawRecord(new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes());
					updated = true;
				}
				if(hr.getDeleted() == null && !mrFactory.getMetadataRecord(hr).matchFilter()){
					hr.setDeleted(new Date());
					updated = true;
				}
				if(updated){
					hr.setUpdated(new Date());
					hrDao.persist(hr);
				}
			}
			catch(Exception ex){
				logger.error(String.format("Exception thrown when filtering harvested_record with id=%s", uniqueId), ex);
			}
		}	
	}
}
