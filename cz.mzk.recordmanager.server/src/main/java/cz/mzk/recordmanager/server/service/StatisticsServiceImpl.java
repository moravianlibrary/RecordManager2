package cz.mzk.recordmanager.server.service;


import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.ActualStatisticsDto;
import cz.mzk.recordmanager.api.model.statistics.IndexAllRecordsJobStatisticsDto;
import cz.mzk.recordmanager.api.model.statistics.OaiHarvestJobStatisticsDto;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.Date;
import java.util.List;

public class StatisticsServiceImpl implements StatisticsService{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<ActualStatisticsDto> getActualStatisticsForThePeriod(Date starDate) {
		return jdbcTemplate.query("SELECT bje.job_instance_id, bji.job_name, bje.status, bje.exit_message, bje.start_time\n" +
				"FROM batch_job_execution bje\n" +
				"JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id\n" +
				"WHERE bje.start_time >= ?", new BeanPropertyRowMapper<ActualStatisticsDto>(ActualStatisticsDto.class), starDate);
	}

	@Override
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset) {
		return jdbcTemplate.query("SELECT * FROM oai_harvest_job_stat " +
						"ORDER BY start_time DESC " +
						"LIMIT 10 OFFSET ?",
				new BeanPropertyRowMapper<OaiHarvestJobStatisticsDto>(
						OaiHarvestJobStatisticsDto.class), offset);
	}

	@Override
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo) {
		return jdbcTemplate.query("SELECT * FROM oai_harvest_job_stat " +
						"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (oai_harvest_job_stat.from_param >= ? OR from_param IS NULL ) AND (oai_harvest_job_stat.to_param <= ? OR to_param IS NULL )" +
						"ORDER BY start_time DESC ",
				new BeanPropertyRowMapper<OaiHarvestJobStatisticsDto>(
						OaiHarvestJobStatisticsDto.class),startEnd.getStart(), startEnd.getEnd(), fromTo.getStart(), fromTo.getEnd());
	}

	@Override
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * " +
				"FROM index_all_records " +
				"LIMIT 10 " +
				"OFFSET ?", new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(IndexAllRecordsJobStatisticsDto.class), offset);
	}
}
