package cz.mzk.recordmanager.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class HarvestedRecordRowMapper implements RowMapper<HarvestedRecord> {

	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao; 

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(oaiHarvestConfigurationDao.load(rs.getLong("oai_harvest_conf_id")), rs.getString("record_id"));
		HarvestedRecord record = new HarvestedRecord(id);
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		return record;
	}

}
