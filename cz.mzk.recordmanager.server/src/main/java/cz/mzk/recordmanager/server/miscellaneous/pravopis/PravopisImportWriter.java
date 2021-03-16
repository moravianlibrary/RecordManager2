package cz.mzk.recordmanager.server.miscellaneous.pravopis;

import cz.mzk.recordmanager.server.model.Pravopis;
import cz.mzk.recordmanager.server.oai.dao.hibernate.PravopisDAOHibernate;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class PravopisImportWriter implements ItemWriter<List<Pravopis>> {

	private static Logger logger = LoggerFactory.getLogger(PravopisImportWriter.class);

	@Autowired
	private PravopisDAOHibernate pravopisDAOHibernate;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends List<Pravopis>> list) throws Exception {
		for (List<Pravopis> pravopis : list) {
			for (Pravopis item : pravopis) {
				if (item.getKey().equals(item.getValue())) continue;
				pravopisDAOHibernate.saveOrUpdate(item);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}
}
