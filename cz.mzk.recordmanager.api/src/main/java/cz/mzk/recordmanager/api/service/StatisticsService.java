package cz.mzk.recordmanager.api.service;


import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.*;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
	List<ActualStatisticsDto> getActualStatisticsForThePeriod(Date startDate);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo, List<LibraryDto> libraries);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(Integer offset);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo);

	List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset);

	List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(PeriodDto startEnd);

	StatisticDetailsDto getDetails(Long jobExecutionId);

	List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(Integer offset);

	List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(PeriodDto startEnd);

	List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(Integer offset);

	List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatisticsInPeriod(PeriodDto startEnd);
}
