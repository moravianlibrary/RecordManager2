package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.batch.OaiHarvestJobStatisticsDto;

import java.util.List;


public interface OaiHarvestStatisticsService {
    List<OaiHarvestJobStatisticsDto> getHarvestJobStatistics();
}
