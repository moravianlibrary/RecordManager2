package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;
import cz.mzk.recordmanager.server.oai.dao.BatchJobExecutionDAO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.batch.runtime.BatchStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 10/11/16.
 */
public class BatchServiceImpl implements BatchService {
    @Autowired
    private BatchJobExecutionDAO batchJobExecutionDAO;
    @Override
    public List<BatchJobExecutionDTO> getRunningJobExecutions() {
            List<BatchJobExecution> jobs = batchJobExecutionDAO.findAll();
        List<BatchJobExecutionDTO> runningJobs = new ArrayList<>();

        jobs.forEach(job -> {
            if (job.getStatus().equals("STARTED") && job.getExitCode().equals("UNKNOWN"))
            {
                runningJobs.add(translate(job));
            }
        });

        return runningJobs;
    }

    @Override
    public BatchJobExecutionDTO getJobExecution(long id) {
        return null;
    }

    @Override
    public void restart(BatchJobExecutionDTO jobExecution) {
    }

    private BatchJobExecutionDTO translate(BatchJobExecution execution)
    {
        BatchJobExecutionDTO dto = new BatchJobExecutionDTO();

        dto.setId(execution.getId());
        return dto;
    }
}
