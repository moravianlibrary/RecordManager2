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
import cz.mzk.recordmanager.server.dc.InvalidDcException;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.FulltextMonographyDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class KrameriusFulltextProcessor implements
		ItemProcessor<HarvestedRecord, HarvestedRecord>, StepExecutionListener {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltextProcessor.class);

	@Autowired
	private KrameriusFulltexterFactory krameriusFulltexterFactory;

	private KrameriusFulltexter fulltexter;

	@Autowired
	private KrameriusConfigurationDAO configDao;

	@Autowired
	private HarvestedRecordDAO recordDao;

	@Autowired
	private FulltextMonographyDAO fmDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private DublinCoreParser parser;

	// configuration
	private Long confId;

	private boolean downloadPrivateFulltexts;

	public KrameriusFulltextProcessor(Long confId) {
		super();
		this.confId = confId;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = sync.register()) {
			KrameriusConfiguration config = configDao.get(confId);
			downloadPrivateFulltexts = configDao.get(confId)
					.isDownloadPrivateFulltexts();
			fulltexter = krameriusFulltexterFactory.create(config);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public HarvestedRecord process(HarvestedRecord item) throws Exception {
		logger.debug("Processing Harvested Record: " + item.toString()
				+ " uniqueId: " + item.getUniqueId());

		String policy;
		// read complete HarvestedRecord using DAO
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(item
				.getUniqueId().getRecordId(), confId);
		
		
		InputStream is = new ByteArrayInputStream(rec.getRawRecord());
		// get Kramerius policy from record
		try {
			DublinCoreRecord dcRecord = parser.parseRecord(is);
			MetadataDublinCoreRecord mdrc = new MetadataDublinCoreRecord(dcRecord);
			policy = mdrc.getPolicy();
		} catch ( InvalidDcException e) {
			logger.warn("InvalidDcException for record with id:" + item.getUniqueId());
			logger.warn(e.getMessage());
			//doesn't do anything, just returns rec from DAO and writes a message into log
			return rec;
		}

		// modify read HarvestedRecord only if following condition is fulfilled
		if (policy.equals("public") || downloadPrivateFulltexts) {
			logger.debug("Processor: privacy condition fulfilled, reading pages");

			String rootUuid = rec.getUniqueId().getRecordId();
			List<FulltextMonography> pages = fulltexter
					.getFulltextObjects(rootUuid);

			// if we got empty list in pages => do nothing, return original record
			if (pages.isEmpty()) {
				return rec;
			}
			
			//delete old FulltextMonography from database before adding new ones
			if (!rec.getFulltextMonography().isEmpty()) {
				for (FulltextMonography fm: rec.getFulltextMonography()) {
					fmDao.delete(fm);
				}
			}
			
			rec.setFulltextMonography(pages);
		} else {
			logger.debug("Processor: privacy condition is NOT fulfilled, skipping record");
		}

		return rec;
	}

}
