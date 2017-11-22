package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.jdbc.BlobToStringValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class FulltextKrameriusDAOHibernate extends
		AbstractDomainDAOHibernate<Long, FulltextKramerius> implements
		FulltextKrameriusDAO {

	private static final RowMapper<String> ROW_MAPPER = new BlobToStringValueRowMapper();

	private String fullTextSizeQuery = ResourceUtils.asString("sql/query/FullTextSizeQueryByDedupRecord.sql");

	private String fullTextQuery = ResourceUtils.asString("sql/query/FullTextQueryByDedupRecord.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public long getFullTextSize(DedupRecord record) {
		return jdbcTemplate.queryForObject(fullTextSizeQuery, Collections.singletonMap("dedupRecordId", record.getId()), Long.class);
	}

	@Override
	public List<String> getFullText(DedupRecord record) {
		return jdbcTemplate.query(fullTextQuery, Collections.singletonMap("dedupRecordId", record.getId()), ROW_MAPPER);
	}

	@Override
	public int deleteFulltext(long hr_id) {
		return jdbcTemplate.update("DELETE FROM fulltext_kramerius fk WHERE fk.harvested_record_id = :harvestedRecordId",
				Collections.singletonMap("harvestedRecordId", hr_id));
	}

}
