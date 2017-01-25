package cz.mzk.recordmanager.api.service;


import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.*;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
	List<ActualStatisticsDto> getActualStatisticsForThePeriod(Date startDate);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset);

	List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(Integer offset);

	List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo);

	List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset);

	List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(PeriodDto startEnd);

	StatisticDetailsDto getDetails(Long jobExecutionId);
}
