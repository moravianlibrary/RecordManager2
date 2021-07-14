package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class KramAvailabilityWriter implements ItemWriter<List<KramAvailability>> {

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends List<KramAvailability>> list) throws Exception {
		for (List<KramAvailability> kramAvailabilities : list) {
			for (KramAvailability newItem : kramAvailabilities) {
				KramAvailability oldItem = kramAvailabilityDAO.getByConfigAndUuid(newItem.getHarvestedFrom(), newItem.getUuid());
				if (oldItem == null) { // new
					newItem.setUpdated(new Date());
					newItem.setLastHarvest(new Date());
					getPageValues(newItem);
					kramAvailabilityDAO.saveOrUpdate(newItem);
					continue;
				}
				if (isUpdated(oldItem, newItem)) {
					kramAvailabilityDAO.dropKeys(oldItem);
					oldItem.setAvailability(newItem.getAvailability());
					oldItem.setDnnt(newItem.isDnnt());
					oldItem.setDnntLabels(newItem.getDnntLabels());
					oldItem.setUpdated(new Date());
					oldItem.setIssn(newItem.getIssn());
					oldItem.setIssue(newItem.getIssue());
					oldItem.setVolume(newItem.getVolume());
					oldItem.setYaer(newItem.getYaer());
					oldItem.setPage(newItem.getPage());
					getPageValues(oldItem);
				}
				oldItem.setLevel(newItem.getLevel());
				oldItem.setLastHarvest(new Date());
				kramAvailabilityDAO.saveOrUpdate(oldItem);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private static boolean isUpdated(final KramAvailability oldItem, final KramAvailability newItem) {
		return !oldItem.equals(newItem);
	}

	private void getPageValues(final KramAvailability page) {
		if (!page.getType().equals("page")) return;
		KramAvailability issue = kramAvailabilityDAO.getByConfigAndUuid(page.getHarvestedFrom(), page.getParentUuid());
		if (issue == null) return;
		page.setIssue(issue.getIssue());
		page.setIssn(issue.getIssn());
		KramAvailability volume = kramAvailabilityDAO.getByConfigAndUuid(page.getHarvestedFrom(), issue.getParentUuid());
		if (volume == null) return;
		page.setVolume(volume.getVolume());
		if (!page.getIssn().isEmpty() && page.getYaer() != null && page.getVolume() != null && page.getIssue() != null
				&& page.getPage() != null) {
			page.setDedupKey(StringUtils.join(new String[]{page.getIssn(), page.getYaer().toString(),
					page.getVolume().toString(), page.getIssue().toString(), page.getPage().toString()}, ";"));
		}
	}

}
