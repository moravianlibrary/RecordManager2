package cz.mzk.recordmanager.server.service;


import cz.mzk.recordmanager.api.model.statistics.ActualStatisticsDto;
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
}
