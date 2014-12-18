package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Component
@StepScope
public class OAIItemWriter implements ItemWriter<List<OAIRecord>>, StepExecutionListener {

	@Autowired
	protected HarvestedRecordDAO recordDao;
	
	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	private OAIHarvestConfiguration configuration; 

	@Override
	public void write(List<? extends List<OAIRecord>> items) throws Exception {
		for (List<OAIRecord> records : items) {
			for (OAIRecord record : records) {
				write(record);
			}
		}
	}
	
	protected void write(OAIRecord record) {
		HarvestedRecord rec = new HarvestedRecord();
		rec.setOaiRecordId(record.getHeader().getIdentifier());
		rec.setHarvestedFrom(configuration);
		recordDao.persist(rec);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		Long confId = stepExecution.getJobParameters().getLong("configurationId");
		configuration = configDao.get(confId);
	}

}
