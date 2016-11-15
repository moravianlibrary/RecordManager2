package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.OaiHarvestJobStatisticsDto;
import cz.mzk.recordmanager.api.service.OaiHarvestStatisticsService;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public class OaiHarvestStatisticsServiceImpl implements OaiHarvestStatisticsService {


    @Autowired
    private JdbcTemplate jdbcTemplate;

	@Autowired
	private ImportConfigurationDAO importConfigurationDAO;

    @Override
    public List<OaiHarvestJobStatisticsDto> getHarvestJobStatistics() {
         List statistics = jdbcTemplate.query("SELECT\n" +
		        "  bje.job_execution_id,\n" +
		        "  (array_agg(ic.id))[1]  import_conf_id,\n" +
		        "  l.name library_name,\n" +
		        "  ohc.url url,\n" +
		        "  ohc.set_spec,\n" +
		        "  bje.start_time,\n" +
		        "  bje.end_time,\n" +
		        "  bje.status,\n" +
		        "  from_param.date_val from_param,\n" +
		        "  to_param.date_val to_param,\n" +
		        "  (SELECT sum(read_count) FROM batch_step_execution bse WHERE bse.job_execution_id = bje.job_execution_id) no_of_records\n" +
		        "FROM batch_job_instance bji\n" +
		        "  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id\n" +
		        "  JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'\n" +
		        "  LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'\n" +
		        "  LEFT JOIN batch_job_execution_params from_param ON from_param.job_execution_id = bje.job_execution_id AND from_param.key_name = 'from'\n" +
		        "  LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val\n" +
		        "  LEFT JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val\n" +
		        "  JOIN import_conf ic ON ic.id = ohc.import_conf_id OR ic.id = kc.import_conf_id\n" +
		        "  JOIN library l ON l.id = ic.library_id\n" +
		        "WHERE bji.job_name IN ('oaiHarvestJob', 'oaiReharvestJob', 'oaiPartitionedHarvestJob', 'cosmotronHarvestJob', 'krameriusHarvestJob', 'krameriusHarvestNoSortingJob')\n" +
		        "GROUP BY bje.job_execution_id,l.name,ohc.url,ohc.set_spec,from_param.date_val,to_param.date_val\n", new BeanPropertyRowMapper(OaiHarvestJobStatisticsDto.class));
        return statistics;
    }

	@Override
	@Transactional(readOnly = true)
	public IdDto getLibraryId(Long configId) {
		IdDto id = new IdDto();
		id.setId(importConfigurationDAO.get(configId).getLibrary().getId());
		return id;
	}
}
