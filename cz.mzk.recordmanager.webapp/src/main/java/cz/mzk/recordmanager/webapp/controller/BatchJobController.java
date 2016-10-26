package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/batches")
public class BatchJobController {

    @Autowired
    private BatchService batchService;


	@RequestMapping(method = RequestMethod.GET, value = "/{jobId}")
	@ResponseBody
	private BatchJobExecutionDTO getJobExecution(@PathVariable Long jobId)
	{
		return batchService.getJobExecution(jobId);
	}

    @RequestMapping(method = RequestMethod.GET, value = "/running")
    @ResponseBody
    private List<BatchJobExecutionDTO> getRunningJobExecutions()
    {
        return batchService.getRunningJobExecutions();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/run/fullHarvest")
	@ResponseBody
	private void runFullHarvest(@RequestBody IdDto configId){
	    batchService.runFullHarvest(configId);
    }

	@RequestMapping(method = RequestMethod.POST, value = "/run/incrementalHarvest")
	@ResponseBody
	private void runIncrementalHarvest(@RequestBody IdDto configId){
		batchService.runIncrementalHarvest(configId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/deduplicate")
	@ResponseBody
	private void runDuduplicate(){
		batchService.runDeduplicate();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/downloadImport")
	@ResponseBody
	private void runDownloadImport(@RequestBody IdDto configId){
		batchService.runDownloadAndImport(configId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/index")
	@ResponseBody
	private void runIndex(){
		batchService.runIndex();
	}
}
