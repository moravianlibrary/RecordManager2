package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.statistics.*;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

public class StatisticsServiceImpl implements StatisticsService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<ActualStatisticsDto> getActualStatisticsForThePeriod(
			Date starDate) {
		return jdbcTemplate
				.query("SELECT bje.job_execution_id, bji.job_name, bje.status, bje.exit_message, bje.start_time\n"
						+ "FROM batch_job_execution bje\n"
						+ "JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id\n"
						+ "WHERE bje.start_time >= ?",
						new BeanPropertyRowMapper<ActualStatisticsDto>(
								ActualStatisticsDto.class), starDate);
	}

	@Override
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestJobStats(Integer offset) {
		return jdbcTemplate.query("SELECT * FROM oai_harvest_job_stat "
				+ "ORDER BY start_time DESC " + "LIMIT 10 OFFSET ?",
				new BeanPropertyRowMapper<OaiHarvestJobStatisticsDto>(
						OaiHarvestJobStatisticsDto.class), offset);
	}

	@Override
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(
			Date start, Date end, Date from, Date to, List<LibraryDto> libraries) {
		if (libraries != null && libraries.size() <= 0) {
			return jdbcTemplate
					.query("SELECT * FROM oai_harvest_job_stat "
							+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (oai_harvest_job_stat.from_param >= ? OR from_param IS NULL ) AND (oai_harvest_job_stat.to_param <= ? OR to_param IS NULL )"
							+ "ORDER BY start_time DESC ",
							new BeanPropertyRowMapper<OaiHarvestJobStatisticsDto>(
									OaiHarvestJobStatisticsDto.class), start,
							end, from, to);
		} else {
			Set<Long> librariesIds = new HashSet<>();
			libraries.forEach(lib -> {
				librariesIds.add(lib.getId());
			});

			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(
					jdbcTemplate);

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("stime", start);
			params.addValue("etime", end);
			params.addValue("fpar", from);
			params.addValue("tpar", to);
			params.addValue("ids", librariesIds);
			return namedParameterJdbcTemplate
					.query("SELECT * FROM oai_harvest_job_stat "
							+ "WHERE (start_time >= :stime OR start_time IS NULL ) AND (end_time <= :etime OR end_time IS NULL ) AND (oai_harvest_job_stat.from_param >= :fpar OR from_param IS NULL ) AND (oai_harvest_job_stat.to_param <= :tpar OR to_param IS NULL ) AND library_id IN (:ids) "
							+ "ORDER BY start_time DESC ",
							params,
							new BeanPropertyRowMapper<OaiHarvestJobStatisticsDto>(
									OaiHarvestJobStatisticsDto.class));
		}

	}

	@Override
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(
			Integer offset) {
		return jdbcTemplate.query("SELECT * " + "FROM index_all_records "
				+ "ORDER BY start_time DESC " + "LIMIT 10 " + "OFFSET ?",
				new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(
						IndexAllRecordsJobStatisticsDto.class), offset);
	}

	@Override
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(
			Date startDate, Date endDate, Date fromDate, Date toDate) {

		if (fromDate.getTime() == 0) {
			return jdbcTemplate
					.query("SELECT * "
							+ "FROM index_all_records "
							+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (from_param IS NULL)"
							+ "ORDER BY start_time DESC ",
							new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(
									IndexAllRecordsJobStatisticsDto.class),
							startDate, endDate);
		} else {
			return jdbcTemplate
					.query("SELECT * "
							+ "FROM index_all_records "
							+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) AND (from_param >= ?) AND ( to_param <= ? OR to_param IS NULL ) "
							+ "ORDER BY start_time DESC ",
							new BeanPropertyRowMapper<IndexAllRecordsJobStatisticsDto>(
									IndexAllRecordsJobStatisticsDto.class),
							startDate, endDate, fromDate, toDate);
		}

	}

	@Override
	public List<DedupRecordsDto> getDedupRecordsStatistics(Integer offset) {
		return jdbcTemplate.query("SELECT * " + "FROM dedup_records_st "
				+ "ORDER BY start_time DESC " + "LIMIT 10 " + "OFFSET ?",
				new BeanPropertyRowMapper<DedupRecordsDto>(
						DedupRecordsDto.class), offset);
	}

	@Override
	public List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(
			Date startDate, Date endDate) {
		return jdbcTemplate
				.query("SELECT * "
						+ "FROM dedup_records_st "
						+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) "
						+ "ORDER BY start_time DESC ",
						new BeanPropertyRowMapper<DedupRecordsDto>(
								DedupRecordsDto.class), startDate, endDate);
	}

	@Override
	public StatisticDetailsDto getDetails(Long jobExecutionId) {

		StatisticDetailsDto detailsDto = jdbcTemplate
				.queryForObject(
						"SELECT"
								+ "  *\n"
								+ "FROM batch_job_execution bje JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id "
								+ "WHERE bje.job_execution_id = ?",
						new BeanPropertyRowMapper<StatisticDetailsDto>(
								StatisticDetailsDto.class), jobExecutionId);

		List<JobParameterDto> jobParametersDto = jdbcTemplate.query("SELECT * "
				+ "FROM batch_job_execution_params "
				+ "WHERE job_execution_id = ?",
				new BeanPropertyRowMapper<JobParameterDto>(
						JobParameterDto.class), jobExecutionId);

		detailsDto.setJobParameter(jobParametersDto);

		return detailsDto;
	}

	@Override
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatistics(
			Integer offset) {
		return jdbcTemplate.query("SELECT * " + "FROM download_import_view "
				+ "ORDER BY start_time DESC " + "LIMIT 10 " + "OFFSET ?",
				new BeanPropertyRowMapper<DownloadImportConfJobStatisticsDto>(
						DownloadImportConfJobStatisticsDto.class), offset);
	}

	@Override
	public List<DownloadImportConfJobStatisticsDto> getDownloadImportConfJobStatisticsInPeriod(
			Date startDate, Date endDate) {
		return jdbcTemplate
				.query("SELECT * "
						+ "FROM download_import_view "
						+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) "
						+ "ORDER BY start_time DESC ",
						new BeanPropertyRowMapper<DownloadImportConfJobStatisticsDto>(
								DownloadImportConfJobStatisticsDto.class),
						startDate, endDate);
	}

	@Override
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatistics(
			Integer offset) {
		return jdbcTemplate.query("SELECT * "
				+ "FROM regenerate_dedup_keys_view "
				+ "ORDER BY start_time DESC " + "LIMIT 10 " + "OFFSET ?",
				new BeanPropertyRowMapper<RegenerateDedupKeysJobStatisticsDto>(
						RegenerateDedupKeysJobStatisticsDto.class), offset);
	}

	@Override
	public List<RegenerateDedupKeysJobStatisticsDto> getRegenerateDedupKeysJobStatisticsInPeriod(
			Date startDate, Date endDate) {
		return jdbcTemplate
				.query("SELECT * "
						+ "FROM regenerate_dedup_keys_view "
						+ "WHERE (start_time >= ? OR start_time IS NULL ) AND (end_time <= ? OR end_time IS NULL ) "
						+ "ORDER BY start_time DESC ",
						new BeanPropertyRowMapper<RegenerateDedupKeysJobStatisticsDto>(
								RegenerateDedupKeysJobStatisticsDto.class),
						startDate, endDate);
	}

}
