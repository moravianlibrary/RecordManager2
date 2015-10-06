package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;


public class KrameriusFulltextProcessor implements ItemProcessor<HarvestedRecord, HarvestedRecord>, StepExecutionListener{
	
	@Autowired
	KrameriusFulltexter kf;
	
	@Autowired
	private KrameriusConfigurationDAO configDao;
	
	@Autowired
	HarvestedRecordDAO recordDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;
	
	@Autowired
	private DublinCoreParser parser;
	
	private static Logger logger = LoggerFactory.getLogger(KrameriusFulltextProcessor.class);
	
	// configuration
	private Long confId;
	private boolean downloadPrivateFulltexts;
	
	
	public KrameriusFulltextProcessor(Long confId) {
		super();
		this.confId = confId;
	}
		
	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			downloadPrivateFulltexts=configDao.get(confId).isDownloadPrivateFulltexts();
			
			kf.setAuthToken(configDao.get(confId).getAuthToken());
			kf.setKramApiUrl(configDao.get(confId).getUrl());
			kf.setDownloadPrivateFulltexts(downloadPrivateFulltexts);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HarvestedRecord process(HarvestedRecord item) throws Exception {
		logger.debug("Processing Harvested Record: " +item.toString() + " uniqueId: "+item.getUniqueId());

		InputStream is = new ByteArrayInputStream(item.getRawRecord());
		// get Kramerius policy from record
		DublinCoreRecord dcRecord = parser.parseRecord(is);
		MetadataDublinCoreRecord mdrc = new MetadataDublinCoreRecord(dcRecord);
		String policy = mdrc.getPolicy();
		
		// read complete HarvestedRecord using DAO
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(item.getUniqueId().getRecordId(), confId);
		
		// modify read HarvestedRecord only if following condition is fulfilled
		if ( policy.equals("public") || downloadPrivateFulltexts ) {		
			logger.debug("Processor: privacy condition fulfilled, reading pages");
	
			String rootUuid = rec.getUniqueId().getRecordId();
			List<FulltextMonography> pages = kf.getFulltextObjects(rootUuid);
			
			rec.setFulltextMonography(pages);	
		} else {
			logger.debug("Processor: privacy condition is NOT fulfilled, skipping record");
		}
		
		return rec;
	}

}
