package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class KramAvailabilityWriter implements ItemWriter<KramAvailability> {

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends KramAvailability> items)
			throws Exception {
		for (KramAvailability newItem : items) {
			KramAvailability oldItem = kramAvailabilityDAO.getByConfigAndUuid(newItem.getHarvestedFrom(), newItem.getUuid());
			if (oldItem == null) { // new
				newItem.setUpdated(new Date());
				newItem.setLastHarvest(new Date());
				kramAvailabilityDAO.saveOrUpdate(newItem);
				continue;
			}
			if (isUpdated(oldItem, newItem)) {
				oldItem.setAvailability(newItem.getAvailability());
				oldItem.setDnnt(newItem.isDnnt());
				oldItem.setUpdated(new Date());
			}
			oldItem.setLevel(newItem.getLevel());
			oldItem.setLastHarvest(new Date());
			kramAvailabilityDAO.saveOrUpdate(oldItem);
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private static boolean isUpdated(final KramAvailability oldItem, final KramAvailability newItem) {
		return !oldItem.getAvailability().equals(newItem.getAvailability()) || oldItem.isDnnt() != newItem.isDnnt();
	}

}
