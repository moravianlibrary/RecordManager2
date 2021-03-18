package cz.mzk.recordmanager.server.miscellaneous.oldspelling;

import cz.mzk.recordmanager.server.model.TitleOldSpelling;
import cz.mzk.recordmanager.server.oai.dao.hibernate.TitleOldSpellingDAOHibernate;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class TitleOldSpellingImportWriter implements ItemWriter<List<TitleOldSpelling>> {

	@Autowired
	private TitleOldSpellingDAOHibernate titleOldSpellingDAOHibernate;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends List<TitleOldSpelling>> list) throws Exception {
		for (List<TitleOldSpelling> titleOldSpellings : list) {
			for (TitleOldSpelling item : titleOldSpellings) {
				if (item.getKey().equals(item.getValue())) continue;
				titleOldSpellingDAOHibernate.saveOrUpdate(item);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}
}
