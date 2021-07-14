package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
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
public class RegenerateKramAvailabilityWriter implements ItemWriter<Long> {

	private static final Logger logger = LoggerFactory.getLogger(RegenerateKramAvailabilityWriter.class);

	@Autowired
	protected KramAvailabilityDAO kramAvailabilityDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	private final ProgressLogger progressLogger = new ProgressLogger(logger, 10000);

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			KramAvailability availability = kramAvailabilityDAO.get(id);

			progressLogger.incrementAndLogProgress();
			KramAvailabilityWriter.getPageValues(kramAvailabilityDAO, availability);
			kramAvailabilityDAO.saveOrUpdate(availability);
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
