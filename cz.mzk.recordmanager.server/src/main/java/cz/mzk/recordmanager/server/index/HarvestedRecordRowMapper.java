package cz.mzk.recordmanager.server.index;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

@Component
public class HarvestedRecordRowMapper implements
		RowMapper<HarvestedRecord> {
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfDao;

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		HarvestedRecordId id = new HarvestedRecordId(oaiHarvestConfDao.load(rs.getLong("oai_harvest_conf_id")), rs.getString("record_id"));
		HarvestedRecord record = new HarvestedRecord(id);
		record.setUpdated(rs.getDate("updated"));
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		return record;
	}

}
