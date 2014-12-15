package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Component
@Scope("step")
public class OAIItemReader implements ItemReader<List<OAIRecord>>, ItemStream, StepExecutionListener {

	// configuration
	private String url;

	private String metadataPrefix;

	private String set;

	private Date from;

	private Date until;

	// state
	private String resumptionToken;

	@Autowired
	private OAIHarvester harvester;
	
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
		url = stepExecution.getJobParameters().getString("url");
		metadataPrefix = stepExecution.getJobParameters().getString("metadataPrefix");
		set = stepExecution.getJobParameters().getString("set");
		from = stepExecution.getJobParameters().getDate("from");
		until = stepExecution.getJobParameters().getDate("until");
		harvester = new OAIHarvester(url, metadataPrefix, set, from, until);
	}

}
