package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

@Component
public class HarvestedRecordLimitedRowMapper implements RowMapper<HarvestedRecord> {
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao; 

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(oaiHarvestConfigurationDao.load(
				rs.getLong("IMPORT_CONF_ID")), 
				rs.getString("RECORD_ID"));
		HarvestedRecord harvestedRecord = new HarvestedRecord(id);
		harvestedRecord.setPublicationYear(rs.getLong("PUBLICATION_YEAR"));
		harvestedRecord.setFormat(rs.getString("FORMAT"));
		return harvestedRecord;
	}
	
	public HarvestedRecordLimitedRowMapper() {
		// TODO Auto-generated constructor stub
	}
}
