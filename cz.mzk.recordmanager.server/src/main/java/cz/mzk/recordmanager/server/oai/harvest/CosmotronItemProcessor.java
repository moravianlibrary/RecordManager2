package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.CosmotronUtils;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
public class CosmotronItemProcessor implements ItemProcessor<List<OAIRecord>, List<HarvestedRecord>>, StepExecutionListener {

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	protected Cosmotron996DAO cosmotronDao;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;
	
	private String format;
	
	private OAIHarvestConfiguration configuration;

	private Transformer transformer;
	
	private Map<String, HarvestedRecord> results;
	
	private Map<String, List<Cosmotron996>> temp996;

	PrintWriter writer;

	public CosmotronItemProcessor(){
	}

	public CosmotronItemProcessor(String deletedOutFile) {
		super();
		temp996 = new HashMap<>();
		createWriter(deletedOutFile);
	}

	@Override
	public List<HarvestedRecord> process(List<OAIRecord> arg0) throws Exception {
		results = new HashMap<>();
		for (OAIRecord oaiRec: arg0) {
			results.put(extractIdentifier(oaiRec.getHeader().getIdentifier()), createHarvestedRecord(oaiRec));
		}

		return new ArrayList<HarvestedRecord>(results.values());
	}
	
	protected HarvestedRecord createHarvestedRecord(OAIRecord record) throws TransformerException {
		String recordId = extractIdentifier(record.getHeader().getIdentifier());
		HarvestedRecord rec;
		if(record.getHeader().isDeleted()){ // is deleted
			rec = recordDao.findByIdAndHarvestConfiguration(recordId, configuration);
			if(rec != null){ // is harvested record
				rec.setDeleted(new Date());
				rec.setUpdated(new Date());
				rec.setRawRecord(new byte[0]);
			}
			else{
				Cosmotron996 c996 = cosmotronDao.findByIdAndHarvestConfiguration(recordId, configuration);
				if(c996 != null){ // is 996 record
					HarvestedRecord parentHr = results.get(c996.getHarvestedRecord().getUniqueId().getRecordId());
					if(parentHr == null) parentHr = c996.getHarvestedRecord();
					c996.setUpdated(new Date());
					c996.setDeleted(new Date());
					c996.setRawRecord(new byte[0]);
					c996.setHarvestedRecord(parentHr);
					rec = CosmotronUtils.update996(parentHr, c996);
				}
				else{ // others
					// create new record
					HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
					rec = new HarvestedRecord(id);
					rec.setHarvestedFrom(configuration);
					rec.setFormat(format);
					rec.setUpdated(new Date());
					rec.setDeleted(new Date());
					rec.setRawRecord(new byte[0]);
				}
			}
		}
		else{ // not deleted
			byte[] recordContent = asByteArray(record.getMetadata().getElement());
			if (configuration.isInterceptionEnabled()) {
				MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(configuration,recordContent);
				if (interceptor != null) {
					//in case of invalid MARC is error processed later
					recordContent = interceptor.intercept();
				}
			}
			InputStream is = new ByteArrayInputStream(recordContent);
			MarcRecord mr = marcXmlParser.parseRecord(is);
			
			String recordIdFrom773 = CosmotronUtils.get77308w(mr);
			List<DataField> all996 = get996(mr);
			if(recordIdFrom773 != null){ // exist field 773 08$w
				if(all996 != null && !all996.isEmpty()){ // exist field 996
					Cosmotron996 new996 = new Cosmotron996();										
					new996.setUpdated(new Date());
					new996.setRecordId(recordId);
					new996.setHarvestedFrom(configuration.getId());
					new996.setRawRecord(recordContent);
					
					HarvestedRecord parentHr = results.get(recordIdFrom773);
					if(parentHr == null) parentHr = recordDao.findByIdAndHarvestConfiguration(recordIdFrom773, configuration);
					if(parentHr == null) {
						List<Cosmotron996> tempList = temp996.get(recordIdFrom773);
						if(tempList == null) {
							temp996.put(recordIdFrom773, new ArrayList<>(Collections.singletonList(new996)));
						}
						else {
							tempList.add(new996);
							temp996.put(recordIdFrom773, tempList);
						}
						return null;					
					}
					else{
						new996.setHarvestedRecord(parentHr);
						rec = CosmotronUtils.update996(parentHr, new996);
					}
				}				
				else{ // not stored records - 77308$w && not 996
					if(writer != null) writer.println(mr.export(IOFormat.LINE_MARC));
					return null;
				}
			}
			else{ // not field 773
				rec = recordDao.findByIdAndHarvestConfiguration(recordId, configuration);
				if (rec == null) {
					// create new record
					HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
					rec = new HarvestedRecord(id);
					rec.setHarvestedFrom(configuration);
					rec.setFormat(format);
					if(temp996.containsKey(recordId)){
						rec.setRawRecord(new byte[0]);
						rec = recordDao.persist(rec);
						rec.setCosmotron(setHarvestedRecordTo996(rec, temp996.remove(recordId)));
					}
				}
				rec.setUpdated(new Date());
				rec.setRawRecord(recordContent);
			}			
		}
		if (record.getHeader().getDatestamp() != null) {
			rec.setTemporalOldOaiTimestamp(rec.getOaiTimestamp());
			rec.setOaiTimestamp(record.getHeader().getDatestamp());
		}		
		if(rec.getDeleted() != null) return rec;
		return updateMarc(rec);
	}
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if(writer != null){
			writer.println("*** 996 records with no parent");
			for(List<Cosmotron996> list996: temp996.values()){
				for(Cosmotron996 c996: list996){
					MarcRecord mr = marcXmlParser.parseRecord(new ByteArrayInputStream(c996.getRawRecord()));
					writer.println(mr.export(IOFormat.LINE_MARC));
				}
			}
			
			writer.close();
		}
		
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
			Long confId = stepExecution.getJobParameters().getLong(
					"configurationId");
			configuration = configDao.get(confId);
			format = formatResolver.resolve(configuration.getMetadataPrefix());
			try {
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException tce) {
				throw new RuntimeException(tce);
			}
		}
	}
	
	protected byte[] asByteArray(Element element) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new DOMSource(element), result);
		return bos.toByteArray();
	}
	
	protected List<DataField> get996(MarcRecord mr){
		return mr.getDataFields("996");
	}
		
	protected String extractIdentifier(String oaiIdentifier) {
		if (oaiIdentifier == null) {
			return null;
		}
		
		String[] parts = oaiIdentifier.split(":");
		if (parts.length == 3) {
			return parts[2].replaceAll("/", Constants.COSMOTRON_RECORD_ID_CHAR);
		}
		
		return oaiIdentifier;
	}
	
	protected HarvestedRecord updateMarc(HarvestedRecord hr){
		
		List<Cosmotron996> all996 = hr.getCosmotron();
		
		if(all996 == null || all996.isEmpty()) return hr;
		
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		Record record = marcXmlParser.parseUnderlyingRecord(is);
		MarcRecord marcRecord = new MarcRecordImpl(record);
		Record newRecord = new RecordImpl();
		
		newRecord.setLeader(marcRecord.getLeader());
		for(ControlField cf: record.getControlFields()){
			newRecord.addVariableField(cf);
		}
		
		Map<String, List<DataField>> dfMap = marcRecord.getAllFields();
		for(String tag: new TreeSet<String>(dfMap.keySet())){ // sorted tags
			for(DataField df: dfMap.get(tag)){
				// kill fields 996, 910 and 540
				if(df.getTag().equals("996")) continue;
				else{
					newRecord.addVariableField(df);
				}
			}
		}
		
		for(Cosmotron996 c996: all996){
			if(c996.getDeleted() != null) continue;
			InputStream is996 = new ByteArrayInputStream(c996.getRawRecord());
			Record record996 = marcXmlParser.parseUnderlyingRecord(is996);
			MarcRecord marcRecord996 = new MarcRecordImpl(record996);
			
			for(DataField df: get996(marcRecord996)){
				newRecord.addVariableField(df);
			}
		}
		
		hr.setRawRecord(new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes());
		
		return hr;
	}

	protected List<Cosmotron996> setHarvestedRecordTo996(HarvestedRecord hr, List<Cosmotron996> all996){
		List<Cosmotron996> result = new ArrayList<>();
		for(Cosmotron996 c996: all996){
			c996.setHarvestedRecord(hr);
			result.add(c996);
		}
		return result;		
	}

	private void createWriter(String deletedOutFile){
		try {
			if(deletedOutFile != null) writer = new PrintWriter(deletedOutFile, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
