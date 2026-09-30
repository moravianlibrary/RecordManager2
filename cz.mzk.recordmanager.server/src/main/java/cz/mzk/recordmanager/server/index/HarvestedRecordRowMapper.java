package cz.mzk.recordmanager.server.index;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

@Component
public class HarvestedRecordRowMapper implements
		RowMapper<HarvestedRecord> {
	
	@Autowired
	private ImportConfigurationDAO importConfDao;

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		// records are processed asynchronously in other threads (AsyncItemProcessor), lazy loading there
		// would concurrently modify this (step's) Hibernate session and corrupt it, so use get (no proxy)
		// and initialize lazy associations used in indexing here - add any newly used ones
		ImportConfiguration importConfig = importConfDao.get(rs.getLong("import_conf_id"));
		Hibernate.initialize(importConfig.getSiglaAlls());
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(importConfig, rs.getString("record_id"));
		HarvestedRecord record = new HarvestedRecord(id);
		record.setId(rs.getLong("id"));
		record.setHarvestedFrom(importConfig);
		record.setUpdated(rs.getDate("updated"));
		record.setDeleted(rs.getDate("deleted"));
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		return record;
	}

}
