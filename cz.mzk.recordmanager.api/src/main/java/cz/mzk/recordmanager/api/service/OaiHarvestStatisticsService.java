package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.OaiHarvesrJobStatisticsDto;

import java.util.List;

public interface OaiHarvestStatisticsService {

	public List<OaiHarvesrJobStatisticsDto> getHarvestJobStatistics(Integer offset);

	public IdDto getLibraryId(Long configId);

}
