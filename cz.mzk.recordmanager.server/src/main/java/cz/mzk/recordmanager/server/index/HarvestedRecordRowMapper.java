package cz.mzk.recordmanager.server.index;

import java.sql.ResultSet;
import java.sql.SQLException;

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
		ImportConfiguration importConfig = importConfDao.load(rs.getLong("import_conf_id"));
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(importConfig, rs.getString("record_id"));
		HarvestedRecord record = new HarvestedRecord(id);
		record.setHarvestedFrom(importConfig);
		record.setUpdated(rs.getDate("updated"));
		record.setDeleted(rs.getDate("deleted"));
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		return record;
	}

}
