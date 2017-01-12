package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.OaiHarvesrJobStatisticsDto;
import cz.mzk.recordmanager.api.service.OaiHarvestStatisticsService;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class OaiHarvestStatisticsServiceImpl implements
		OaiHarvestStatisticsService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ImportConfigurationDAO importConfigurationDAO;

	@Override
	public List<OaiHarvesrJobStatisticsDto> getHarvestJobStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * FROM oai_harvest_job_stat LIMIT 10 OFFSET ?",
				new BeanPropertyRowMapper<OaiHarvesrJobStatisticsDto>(
						OaiHarvesrJobStatisticsDto.class), offset);
	}

	@Override
	@Transactional(readOnly = true)
	public IdDto getLibraryId(Long configId) {
		IdDto id = new IdDto();
		id.setId(importConfigurationDAO.get(configId).getLibrary().getId());
		return id;
	}

}
