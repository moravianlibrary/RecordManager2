package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.statistics.*;

import java.util.Date;
import java.util.List;

public interface StatisticsService {

	List<ActualStatisticsDto> getActualStatisticsForThePeriod(
			Date startDate);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(
			Date start, Date end, Date from, Date to, List<LibraryDto> libraries);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(
			Integer offset);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(
			Date startDate, Date endDate, Date fromDate, Date toDate);

	List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset);

	List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(
			Date startDate, Date endDate);

	StatisticDetailsDto getDetails(Long jobExecutionId);

	List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(
			Integer offset);

	List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(
			Date startDate, Date endDate);

	List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(
			Integer offset);

	List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatisticsInPeriod(
			Date startDate, Date endDate);

}
