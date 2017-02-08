package cz.mzk.recordmanager.server.service;


import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.*;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.hibernate.Criteria;
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
		return jdbcTemplate.query("SELECT bje.job_execution_id, bji.job_name, bje.status, bje.exit_message, bje.start_time\n" +
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
				"ORDER BY start_time DESC " +
				"LIMIT 10 " +
				"OFFSET ?", new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(IndexAllRecordsJobStatisticsDto.class), offset);
	}

	@Override
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(PeriodDto startEnd, PeriodDto fromTo) {

		if (fromTo.getStart() == null){
			return jdbcTemplate.query("SELECT * " +
					"FROM index_all_records " +
					"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (from_param IS NULL)" +
					"ORDER BY start_time DESC ", new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(IndexAllRecordsJobStatisticsDto.class), startEnd.getStart(), startEnd.getEnd());
		}else {
			return jdbcTemplate.query("SELECT * " +
					"FROM index_all_records " +
					"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (from_param >= ?) AND ( to_param <= ? OR to_param IS NULL ) " +
					"ORDER BY start_time DESC ", new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(IndexAllRecordsJobStatisticsDto.class), startEnd.getStart(), startEnd.getEnd(), fromTo.getStart(), fromTo.getEnd());
		}

	}

	@Override
	public List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * " +
				"FROM dedup_records_st " +
				"ORDER BY start_time DESC " +
				"LIMIT 10 " +
				"OFFSET ?", new BeanPropertyRowMapper<DedupRecordsDto>(DedupRecordsDto.class), offset);
	}

	@Override
	public List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(PeriodDto startEnd) {
		return jdbcTemplate.query("SELECT * " +
				"FROM dedup_records_st " +
				"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) " +
				"ORDER BY start_time DESC ", new BeanPropertyRowMapper<DedupRecordsDto>(DedupRecordsDto.class), startEnd.getStart(), startEnd.getEnd());
	}

	@Override
	public StatisticDetailsDto getDetails(Long jobExecutionId) {

		StatisticDetailsDto detailsDto =  jdbcTemplate.queryForObject("SELECT" +
				"  *\n" +
				"FROM batch_job_execution bje JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id " +
				"WHERE bje.job_execution_id = ?", new BeanPropertyRowMapper<StatisticDetailsDto>(StatisticDetailsDto.class), jobExecutionId);

		List<JobParameterDto> jobParametersDto =
				jdbcTemplate.query("SELECT * " +
						"FROM batch_job_execution_params " +
						"WHERE job_execution_id = ?" , new BeanPropertyRowMapper<JobParameterDto>(JobParameterDto.class), jobExecutionId);

		detailsDto.setJobParameter(jobParametersDto);

		return detailsDto;
	}

	@Override
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * " +
				"FROM download_import_view " +
				"ORDER BY start_time DESC " +
				"LIMIT 10 " +
				"OFFSET ?", new BeanPropertyRowMapper<DownloadImportConfJobStatisticsDto>(DownloadImportConfJobStatisticsDto.class), offset);
	}

	@Override
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(PeriodDto startEnd) {
		return jdbcTemplate.query("SELECT * " +
				"FROM download_import_view " +
				"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) " +
				"ORDER BY start_time DESC ", new BeanPropertyRowMapper<DownloadImportConfJobStatisticsDto>(DownloadImportConfJobStatisticsDto.class), startEnd.getStart(), startEnd.getEnd());
	}

	@Override
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * " +
				"FROM regenerate_dedup_keys_view " +
				"ORDER BY start_time DESC " +
				"LIMIT 10 " +
				"OFFSET ?", new BeanPropertyRowMapper<RegenerateDedupKeysJobStatisticsDto>(RegenerateDedupKeysJobStatisticsDto.class), offset);
	}

	@Override
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatisticsInPeriod(PeriodDto startEnd) {
		return jdbcTemplate.query("SELECT * " +
				"FROM regenerate_dedup_keys_view " +
				"WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) " +
				"ORDER BY start_time DESC ", new BeanPropertyRowMapper<RegenerateDedupKeysJobStatisticsDto>(RegenerateDedupKeysJobStatisticsDto.class), startEnd.getStart(), startEnd.getEnd());
	}
}
