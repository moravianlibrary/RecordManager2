package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

/** 
 * Reader for harvesting of single item specified by ID
 * @author mjtecka
 *
 */

@Component
@StepScope
public class OAIItemSingleReader implements ItemReader<List<OAIRecord>>,
		StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	private OAIHarvester harvester;

	// configuration
	private Long confId;

	private String recordId;

	private boolean finished = false;


	public OAIItemSingleReader(Long confId, String recordId) {
		super();
		this.confId = confId;
		this.recordId = recordId;
	}

	/**
	 * Harvests record by Id
	 * 
	 * @return single record in list or null if nothing is found
	 */
	@Override
	public List<OAIRecord> read() {
		if (finished) {
			return null;
		}
		
		OAIGetRecord getRecord = harvester.getRecord(recordId);
	    	    
		if (getRecord == null) {
			return null;
		} else {
		    /* <MJ.> has to finish on 2nd run.. overkill ;-) */
			finished = true;
		    return Arrays.asList(getRecord.getRecord());
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			OAIHarvestConfiguration conf = configDao.get(confId);
			OAIHarvesterParams params = new OAIHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataPrefix(conf.getMetadataPrefix());
			params.setGranularity(conf.getGranularity());
			params.setSet(conf.getSet());
/*			params.setFrom(fromDate);
			params.setUntil(untilDate);
*/
			harvester = harvesterFactory.create(params);
			processIdentify(conf);
			conf = configDao.get(confId);
			params.setGranularity(conf.getGranularity());
			harvester = harvesterFactory.create(params);
		}
	}

	/**
	 * process Identify request and update stored
	 * {@link OAIHarvestConfiguration}
	 */
	private void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvester.identify();
		conf.setGranularity(OAIGranularity.stringToOAIGranularity(identify
				.getGranularity()));
		configDao.persist(conf);
	}

}
