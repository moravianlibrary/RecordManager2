package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dedup.clustering.TitleClusterable;
import cz.mzk.recordmanager.server.dedup.clustering.NonperiodicalTitleClusterable;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.oai.dao.TitleDAO;

@Component
public class TitleDAOHibernate extends AbstractDomainDAOHibernate<Long, Title>
		implements TitleDAO {

	
	@Override
	@SuppressWarnings("unchecked")
	public List<NonperiodicalTitleClusterable> getTitleForDeduplicationByYear(Long year, int minPages, int maxPages, String lang) {
		Session session = sessionFactory.getCurrentSession();
		return (List<NonperiodicalTitleClusterable>)
				session.createSQLQuery("SELECT id,harvested_record_id,title,isbn,cnb,author_string,pages "
								+ "FROM tmp_titles_for_simmilarity_searching "
								+ "WHERE publication_year = :year and pages BETWEEN :minPages and :maxPages AND lang = :lang")
				.setResultTransformer(new ResultTransformer() {

					private static final long serialVersionUID = 1L;

					@Override
					public Object transformTuple(Object[] tuple, String[] aliases) {
						NonperiodicalTitleClusterable title = new NonperiodicalTitleClusterable();
						for (int i = 0; i < tuple.length; i++) {
							switch (aliases[i]) {
							case "id": title.setId(((BigDecimal)tuple[i]).longValue()); break;
							case "harvested_record_id": title.setHarvestedRecordId(((BigDecimal)tuple[i]).longValue()); break;
							case "title": title.setTitle((String)tuple[i]); break;
							case "author_string": title.setAuthorStr((String)tuple[i]); break;
							case "pages": title.setPages(((BigDecimal)tuple[i]).longValue()); break;
							case "isbn":
								if (tuple[i] != null) {
									title.setIsbn(tuple[i].toString()); break;
								}
							case "cnb": 
								if (tuple[i] != null) {
									title.setCnb((String)tuple[i]); break;
								}
							}
						}
						return title;
					}

					@SuppressWarnings("rawtypes")
					@Override
					public List transformList(List collection) {
						return collection;
					}
				})
						.setParameter("year", year)
						.setParameter("minPages", minPages)
						.setParameter("maxPages", maxPages)
						.setParameter("lang", lang)
				.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<TitleClusterable> getPeriodicalsTitleForDeduplication(Long year) {
		Session session = sessionFactory.getCurrentSession();
		return (List<TitleClusterable>)
				session.createSQLQuery("SELECT harvested_record_id,title,similarity_enabled FROM tmp_periodicals_years WHERE publication_year = :year")
					.setResultTransformer(new ResultTransformer() {
						
						private static final long serialVersionUID = 1L;

						@Override
						public Object transformTuple(Object[] tuple, String[] aliases) {
							TitleClusterable titleClusterable = new TitleClusterable();
							for (int i = 0; i < tuple.length; i++) {
								switch (aliases[i]) {
								case "harvested_record_id": titleClusterable.setId(((BigDecimal)tuple[i]).longValue()); break;
								case "title": titleClusterable.setTitle((String)tuple[i]); break;
									case "similarity_enabled": titleClusterable.setSimilarity_enabled((Boolean)tuple[i]);
								}
							}
							return titleClusterable;
						}

						@SuppressWarnings("rawtypes")
						@Override
						public List transformList(List collection) {
							return collection;
						}
					})
						.setParameter("year", year)
					.list();
	}
}
