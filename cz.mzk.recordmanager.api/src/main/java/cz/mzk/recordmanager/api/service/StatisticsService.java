package cz.mzk.recordmanager.api.service;


import cz.mzk.recordmanager.api.model.statistics.ActualStatisticsDto;

import java.time.Period;
import java.util.Date;
import java.util.List;

public interface StatisticsService {
	List<ActualStatisticsDto> getActualStatisticsForThePeriod(Date startDate);
}
