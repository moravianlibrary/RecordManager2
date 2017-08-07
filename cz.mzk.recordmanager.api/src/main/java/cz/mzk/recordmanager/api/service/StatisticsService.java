package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.statistics.*;

import java.util.Date;
import java.util.List;

public interface StatisticsService {

	public List<ActualStatisticsDto> getActualStatisticsForThePeriod(
			Date startDate);

	public List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset);

	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(
			Date start, Date end, Date from, Date to, List<LibraryDto> libraries);

	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(
			Integer offset);

	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(
			Date startDate, Date endDate, Date fromDate, Date toDate);

	public List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset);

	public List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(
			Date startDate, Date endDate);

	public StatisticDetailsDto getDetails(Long jobExecutionId);

	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(
			Integer offset);

	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(
			Date startDate, Date endDate);

	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(
			Integer offset);

	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatisticsInPeriod(
			Date startDate, Date endDate);

}
