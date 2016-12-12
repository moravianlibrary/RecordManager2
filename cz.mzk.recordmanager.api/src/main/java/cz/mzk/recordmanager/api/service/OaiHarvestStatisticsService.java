package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.OaiHarvestJobStatisticsDto;

import java.util.List;

public interface OaiHarvestStatisticsService {

	public List<OaiHarvestJobStatisticsDto> getHarvestJobStatistics(Integer offset);

	public IdDto getLibraryId(Long configId);

}
