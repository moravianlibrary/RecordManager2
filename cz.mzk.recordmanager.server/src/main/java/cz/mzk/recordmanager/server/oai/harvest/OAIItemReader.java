package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Component
@StepScope
public class OAIItemReader implements ItemReader<List<OAIRecord>>, ItemStream, StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	private OAIHarvester harvester; 
	
	// state
	private String resumptionToken;
	
	@Override
	public List<OAIRecord> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		OAIListRecords listRecords = harvester.listRecords(resumptionToken);
		resumptionToken = listRecords.getNextResumptionToken();
		if (listRecords.getRecords().isEmpty()) {
			return null;
		} else {
			return listRecords.getRecords();
		}
	}

	@Override
	public void close() throws ItemStreamException {
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("resumptionToken")) {
			resumptionToken = ctx.getString("resumptionToken"); 
		}
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putString("resumptionToken", resumptionToken);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		Long confId = stepExecution.getJobParameters().getLong("configurationId");
		OAIHarvestConfiguration conf = configDao.get(confId);
		OAIHarvesterParams params = new OAIHarvesterParams();
		params.setUrl(conf.getUrl());
		params.setMetadataPrefix(conf.getMetadataPrefix());
		harvester = harvesterFactory.create(params);
	}

}
