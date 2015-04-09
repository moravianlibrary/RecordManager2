package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public enum HarvestedRecordLimitedRowMapper implements RowMapper<HarvestedRecord> {
	
	INSTANCE;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao; 

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecordId id = new HarvestedRecordId(oaiHarvestConfigurationDao.load(rs.getLong("oai_harvest_conf_id")), rs.getString("record_id"));
		HarvestedRecord harvestedRecord = new HarvestedRecord(id);
		harvestedRecord.setPublicationYear(rs.getLong("PUBLICATION_YEAR"));
		harvestedRecord.setPhysicalFormat(rs.getString("PHYSICAL_FORMAT"));
		harvestedRecord.setTitle(rs.getString("TITLE"));
		harvestedRecord.setIsbn(rs.getString("ISBN"));
		return harvestedRecord;
	}
}
