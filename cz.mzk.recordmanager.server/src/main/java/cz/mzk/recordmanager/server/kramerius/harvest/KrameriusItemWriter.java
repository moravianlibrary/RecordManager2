package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dedup.DedupKeyParserException;
import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.harvest.OAIFormatResolver;
import cz.mzk.recordmanager.server.oai.harvest.OAIItemWriter;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class KrameriusItemWriter implements ItemWriter<List<HarvestedRecord>>,
		StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(OAIItemWriter.class);

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	protected OAIFormatResolver formatResolver;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	private String format;

	private OAIHarvestConfiguration configuration;

	private Transformer transformer;


	@Override
	public void write(List<? extends List<HarvestedRecord>> items)
			throws Exception {
		System.out.println("------------- write list ---------------");
		for (List<HarvestedRecord> lists: items) {
			for (HarvestedRecord hr: lists) {
				try {
					write(hr);
				} catch (TransformerException te) {
					logger.warn("TransformerException when storing {}: ",
							hr, te);
				}
			}
		}
		
	}
	
	public void write(HarvestedRecord record) throws TransformerException {
		System.out.println("zapisuji HarvestedRecord...");
		String recordId = record.getUniqueId().getRecordId(); //TODO check it! 
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(recordId, configuration);
		if (rec == null) {
	//		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
	//		rec = new HarvestedRecord(id);
			rec=record;
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
		}
		try {
			dedupKeysParser.parse(rec);
			System.out.println("...parsuji HarvestedRecord...");
		} catch (DedupKeyParserException dkpe) {
			logger.error(
					"Dedup keys could not be generated for {}, exception thrown.",
					record, dkpe);
		}

		recordDao.persist(rec);
		
		
	}


	@Override
	public void beforeStep(StepExecution stepExecution) {
		System.out.println("-------------writer before step --------------");
		try (SessionBinder session = sync.register()) {
			Long confId = stepExecution.getJobParameters().getLong(
					"configurationId");
			configuration = configDao.get(confId);
			format = formatResolver.resolve("oai_dc"); //TODO
			try {
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException tce) {
				throw new RuntimeException(tce);
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}


	
	
}
