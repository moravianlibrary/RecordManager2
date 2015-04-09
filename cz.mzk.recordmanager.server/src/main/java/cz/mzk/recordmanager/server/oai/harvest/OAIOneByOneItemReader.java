package cz.mzk.recordmanager.server.oai.harvest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIHeader;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class OAIOneByOneItemReader implements ItemReader<List<OAIRecord>>,
		StepExecutionListener {
	
	@Autowired
	private OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	private HibernateSessionSynchronizer hibernateSync;
	
	@Autowired
	private OAIHarvesterFactory harvesterFactory;
	
	private OAIHarvester harvester;
	
	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;

	// state
	private String resumptionToken;

	private boolean finished = false;
	
	public OAIOneByOneItemReader(Long confId, Date fromDate, Date untilDate) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
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
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			harvester = harvesterFactory.create(params);
			processIdentify(conf);
			conf = configDao.get(confId);
			params.setGranularity(conf.getGranularity());
			harvester = harvesterFactory.create(params);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OAIRecord> read() {
		List<OAIRecord> result = new ArrayList<OAIRecord>();
		for (OAIHeader header : readNextIdentifiers()) {
			OAIGetRecord getRecord = harvester.getRecord(header.getIdentifier());
			if (getRecord != null && getRecord.getRecord() != null) {
				result.add(getRecord.getRecord());
			}
		}
		return result.isEmpty() ? null : result;
	}
	
	protected List<OAIHeader> readNextIdentifiers() {
		if (finished) {
			return Collections.emptyList();
		}

		OAIListIdentifiers listIdentifiers = harvester.listIdentifiers(resumptionToken);
		resumptionToken = listIdentifiers.getNextResumptionToken();
		if (resumptionToken == null) {
			finished = true;
		}
		if (listIdentifiers.getHeaders().isEmpty()) {
			return Collections.emptyList();
		} else {
			return listIdentifiers.getHeaders();
		}
	}  
	
	/**
	 * process Identify request and update stored
	 * {@link OAIHarvestConfiguration}
	 */
	protected void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvester.identify();
		conf.setGranularity(OAIGranularity.stringToOAIGranularity(identify
				.getGranularity()));
		configDao.persist(conf);
	}

}
