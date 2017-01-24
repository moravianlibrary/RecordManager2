package cz.mzk.recordmanager.api.service;


import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.ActualStatisticsDto;
import cz.mzk.recordmanager.api.model.statistics.DedupRecordsDto;
import cz.mzk.recordmanager.api.model.statistics.IndexAllRecordsJobStatisticsDto;
import cz.mzk.recordmanager.api.model.statistics.OaiHarvestJobStatisticsDto;

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
}
