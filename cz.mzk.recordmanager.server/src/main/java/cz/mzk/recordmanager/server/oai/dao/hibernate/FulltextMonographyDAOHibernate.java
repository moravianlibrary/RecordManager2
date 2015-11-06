package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.jdbc.BlobToStringValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.oai.dao.FulltextMonographyDAO;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class FulltextMonographyDAOHibernate extends
		AbstractDomainDAOHibernate<Long, FulltextMonography> implements
		FulltextMonographyDAO {

	private static final RowMapper<String> ROW_MAPPER = new BlobToStringValueRowMapper();

	private String fullTextQuery = ResourceUtils.asString("sql/query/FullTextQueryByDedupRecord.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public List<String> getFullText(DedupRecord record) {
		return jdbcTemplate.query(fullTextQuery, Collections.singletonMap("dedupRecordId", record.getId()), ROW_MAPPER);
	}

}
