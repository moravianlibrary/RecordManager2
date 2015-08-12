package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dedup.TitleForDeduplication;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.oai.dao.TitleDAO;

@Component
public class TitleDAOHibernate extends AbstractDomainDAOHibernate<Long, Title>
		implements TitleDAO {

	
	@Override
	@SuppressWarnings("unchecked")
	public List<TitleForDeduplication> getTitleForDeduplicationByYear(Long year, int minPages, int maxPages, String lang) {
		Session session = sessionFactory.getCurrentSession();
		return (List<TitleForDeduplication>)
				session.createSQLQuery("SELECT id,harvested_record_id,title,isbn,cnb,author_string,pages "
						+ "FROM titles_for_simmilarity_searching_view "
						+ "WHERE publication_year = ? and pages BETWEEN ? and ? AND lang = ?")
				.setResultTransformer(new ResultTransformer() {

					@Override
					public Object transformTuple(Object[] tuple, String[] aliases) {
						TitleForDeduplication title = new TitleForDeduplication();
						for (int i = 0; i < tuple.length; i++) {
							switch (aliases[i]) {
							case "id": title.setId(((BigDecimal)tuple[i]).longValue()); break;
							case "harvested_record_id": title.setHarvestedRecordId(((BigDecimal)tuple[i]).longValue()); break;
							case "title": title.setTitle((String)tuple[i]); break;
							case "author_string": title.setAuthorStr((String)tuple[i]); break;
							case "pages": title.setPages(((BigDecimal)tuple[i]).longValue()); break;
							case "isbn":
								if (tuple[i] != null) {
									title.setIsbn(((BigDecimal)tuple[i]).toString()); break;
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
				.setParameter(0, year)
				.setParameter(1, minPages)
				.setParameter(2, maxPages)
				.setParameter(3, lang)
				.list();
	}
}
