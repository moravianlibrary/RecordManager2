package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.jdbc.BlobToStringValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

	@Override
	public String getPolicy(Long harvested_record_id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(FulltextKramerius.class);
		crit.add(Restrictions.eq("harvestedRecordId", harvested_record_id));
		crit.setProjection(Projections.property("isPrivate"));
		crit.setMaxResults(1);
		Iterator iterator = crit.list().iterator();
		return (iterator.hasNext()) ? (Boolean) iterator.next() ? Constants.DOCUMENT_AVAILABILITY_PROTECTED
				: Constants.DOCUMENT_AVAILABILITY_ONLINE : Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
	}


	@Override
	public boolean isDeduplicatedFulltext(HarvestedRecord record, List<Long> importConfIdForDedup) {
		Session session = sessionFactory.getCurrentSession();

		Integer result = (Integer) session.createQuery(
						"select 1 from FulltextKramerius where harvestedRecordId in " +
								"(select id from HarvestedRecord where uniqueId.harvestedFromId in (:importConfIdForDedup) and uniqueId.harvestedFromId != :harvestedFromId " +
								"and uniqueId.recordId = :recordId )")
				.setParameter("importConfIdForDedup", importConfIdForDedup)
				.setParameter("recordId", record.getUniqueId().getRecordId())
				.setParameter("harvestedFromId", record.getUniqueId().getHarvestedFromId())
				.uniqueResult();
		return result != null;
	}
}
