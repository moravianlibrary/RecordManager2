package cz.mzk.recordmanager.server.oai.harvest;

import cz.mzk.recordmanager.server.util.Constants;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public enum ReharvestJobExecutionDecider implements JobExecutionDecider {

	INSTANCE;

	public static final FlowExecutionStatus REHARVEST_FLOW_STATUS = new FlowExecutionStatus("REHARVEST");

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		String reharvest = jobExecution.getJobParameters().getString(Constants.JOB_PARAM_REHARVEST);
		FlowExecutionStatus next = (Constants.JOB_PARAM_TRUE_VALUE.equals(reharvest)) ?
				REHARVEST_FLOW_STATUS : FlowExecutionStatus.COMPLETED;
		return next;
	}

}
