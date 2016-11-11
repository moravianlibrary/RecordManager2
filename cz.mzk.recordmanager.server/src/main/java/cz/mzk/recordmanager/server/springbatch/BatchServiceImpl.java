package cz.mzk.recordmanager.server.springbatch;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.server.facade.*;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.dao.batch.BatchJobExecutionDAO;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;

@Component
public class BatchServiceImpl implements BatchService {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private BatchJobExecutionDAO batchJobExecutionDao;

	@Autowired
	private BatchDTOTranslator dtoTranslator;

	@Autowired
	private OAIHarvestConfigurationDAO harvestConfigurationDAO;

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDAO;

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private ImportRecordFacade importRecordFacade;

	@Autowired
	private DedupFacade dedupFacade;

	@Autowired
	private IndexingFacade indexingFacade;
	
	@Transactional(readOnly=true)
	@Override
	public List<BatchJobExecutionDTO> getRunningJobExecutions() {
		List<BatchJobExecution> jobs = batchJobExecutionDao.getRunningExecutions();
		List<BatchJobExecutionDTO> dtos = new ArrayList<BatchJobExecutionDTO>();
		for (BatchJobExecution job : jobs) {
			dtos.add(dtoTranslator.translate(job));
		}
		return dtos;
	}
	
	@Override
	public BatchJobExecutionDTO getJobExecution(long id) {
		BatchJobExecution exec = batchJobExecutionDao.get(id);
		if (exec == null) {
			throw new IllegalArgumentException(String.format(
					"No batch job execution with id=%s does not exist", id));
		}
		return dtoTranslator.translate(exec);
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void restart(BatchJobExecutionDTO jobExecution) {
		jobExecutor.restart(jobExecution.getId());
	}

	@Transactional(readOnly=true)
	@Override
	public BatchJobExecutionDTO getJobExecution(Long id) {
		return dtoTranslator.translate(batchJobExecutionDao.get(id));
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void runFullHarvest(List<IdDto> id) {
		id.forEach(item -> {
			harvestingFacade.fullHarvest(item.getId());
		});

	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void runIncrementalHarvest(List<IdDto> id) {
		id.forEach(item -> {
			harvestingFacade.incrementalHarvest(item.getId());
		});
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void runDeduplicate() {
		dedupFacade.deduplicate();
	}


	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void runIndex() {
		indexingFacade.index();
	}


}
