package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.statistics.*;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/statistics")
public class JobsStatisticsController {

	@Autowired
	private StatisticsService statisticsService;

	@RequestMapping(method = RequestMethod.GET, value = "oaiHarvestStatistics/{offset}")
	@ResponseBody
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatistics(
			@PathVariable Integer offset) {
		return statisticsService.getOaiHarvestJobStats(offset);
	}

	@RequestMapping(method = RequestMethod.POST, value = "oaiHarvestStatistics/inPeriods")
	@ResponseBody
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(
			@RequestParam(value = "startDate") Long startDate,
			@RequestParam(value = "endDate") Long endDate,
			@RequestParam(value = "fromDate") Long fromDate,
			@RequestParam(value = "toDate") Long toDate,
			@RequestBody List<LibraryDto> libraries) {

		return statisticsService.getOaiHarvestStatisticsInPeriods(new Date(
				startDate), new Date(endDate), new Date(fromDate), new Date(
				toDate), libraries);

	}

	@RequestMapping(method = RequestMethod.GET, value = "actuals")
	@ResponseBody
	public List<ActualStatisticsDto> getActualStatistics(
			@RequestParam(value = "startDate") Long startDate) {
		return statisticsService.getActualStatisticsForThePeriod(new Date(
				startDate));
	}

	@RequestMapping(method = RequestMethod.GET, value = "indexAllRecordsStatistics/{offset}")
	@ResponseBody
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(
			@PathVariable Integer offset) {
		return statisticsService.getIndexAllRecordsStatistics(offset);
	}

	@RequestMapping(method = RequestMethod.GET, value = "indexAllRecordsStatistics/inPeriods")
	@ResponseBody
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(
			@RequestParam(value = "startDate") Long startDate,
			@RequestParam(value = "endDate") Long endDate,
			@RequestParam(value = "fromDate") Long fromDate,
			@RequestParam(value = "toDate") Long toDate) {
		return statisticsService.getIndexAllRecordsStatisticsInPeriods(
				new Date(startDate), new Date(endDate), new Date(fromDate),
				new Date(toDate));
	}

	@RequestMapping(method = RequestMethod.GET, value = "dedupRecordsStatistics/{offset}")
	@ResponseBody
	public List<DedupRecordsDto> getDedupRecordsStatistics(
			@PathVariable Integer offset) {
		return statisticsService.getDedupRecordsStatistics(offset);
	}

	@RequestMapping(method = RequestMethod.GET, value = "dedupRecordsStatistics/inPeriods")
	@ResponseBody
	public List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(
			@RequestParam(value = "startDate") Long startDate,
			@RequestParam(value = "endDate") Long endDate) {
		return statisticsService.getDedupRecordsStatisticsInPeriods(new Date(
				startDate), new Date(endDate));
	}

	@RequestMapping(method = RequestMethod.GET, value = "details/{jobExecutionId}")
	@ResponseBody
	public StatisticDetailsDto getDetails(@PathVariable Long jobExecutionId) {
		return statisticsService.getDetails(jobExecutionId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "downloadImportConfStatistics/{offset}")
	@ResponseBody
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(
			@PathVariable Integer offset) {
		return statisticsService.getDownloadImportConfJobStatistics(offset);
	}

	@RequestMapping(method = RequestMethod.GET, value = "downloadImportConfStatistics/inPeriods")
	@ResponseBody
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(
			@RequestParam(value = "startDate") Long startDate,
			@RequestParam(value = "endDate") Long endDate) {
		return statisticsService.getDownloadImportConfJobStatisticsInPeriod(
				new Date(startDate), new Date(endDate));
	}

	@RequestMapping(method = RequestMethod.GET, value = "regenerateDedupKeysStatistics/{offset}")
	@ResponseBody
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(
			@PathVariable Integer offset) {
		return statisticsService.getRegenerateDedupKeysJobStatistics(offset);
	}

	@RequestMapping(method = RequestMethod.GET, value = "regenerateDedupKeysStatistics/inPeriods")
	@ResponseBody
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobInPeriod(
			@RequestParam(value = "startDate") Long startDate,
			@RequestParam(value = "endDate") Long endDate) {
		return statisticsService.getRegenerateDedupKeysJobStatisticsInPeriod(
				new Date(startDate), new Date(endDate));
	}

}
