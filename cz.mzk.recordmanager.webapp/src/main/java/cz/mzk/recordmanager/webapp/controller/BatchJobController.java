package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.RecordIdDto;
import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

@RestController
@RequestMapping(value = "/batches")
public class BatchJobController {

	@Autowired
	private BatchService batchService;

	@RequestMapping(method = RequestMethod.GET, value = "/{jobId}")
	@ResponseBody
	private BatchJobExecutionDTO getJobExecution(@PathVariable Long jobId) {
		return batchService.getJobExecution(jobId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/running")
	@ResponseBody
	private List<BatchJobExecutionDTO> getRunningJobExecutions() {
		return batchService.getRunningJobExecutions();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/fullHarvest")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runFullHarvest(@RequestBody List<IdDto> configId) {
		batchService.runFullHarvest(configId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/incrementalHarvest")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runIncrementalHarvest(@RequestBody List<IdDto> configId) {
		batchService.runIncrementalHarvest(configId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/deduplicate")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runDuduplicate() {
		batchService.runDeduplicate();
	}


	@RequestMapping(method = RequestMethod.POST, value = "/run/index")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runIndex() {
		batchService.runIndex();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/indexIndividualIndexesToSolr")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runIndexIndividual(@RequestBody List<RecordIdDto> ids) {
		batchService.runIndividualIndex(ids);
	}

	@RequestMapping(method = RequestMethod.PATCH)
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void restart(@RequestBody BatchJobExecutionDTO jobExecution){
		batchService.restart(jobExecution);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/importRecordsJob")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runImportRecordsJob(@RequestParam("file") MultipartFile file, @RequestParam("format") String format, @RequestParam("id") Long id) throws IOException, NoSuchFileException {
		File convFile = new File(file.getOriginalFilename());
		file.transferTo(convFile);
		if (format.toLowerCase().equals("null")){
			format = null;
		}
		batchService.runImportRecordsJob(id, convFile, format);
		Files.delete(convFile.toPath());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/filterCaslinRecordsJob")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runFilterCaslinRecordsJob() {
		batchService.runFilterCaslinRecordsJob();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/regenerateDedupKeysJob")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void runRegenerateDedupKeysJob() {
		batchService.runRegenerateDedupKeysJob();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/run/regenerateMissingDedupKeysJob")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	private void regenerateMissingDedupKeysJob() {
		batchService.runRegenerateMissingDedupKeysJob();
	}

}
