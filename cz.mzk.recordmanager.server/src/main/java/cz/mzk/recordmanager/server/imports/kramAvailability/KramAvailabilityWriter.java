package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
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
				newItem.setPage(parsePage(newItem.getPage(), newItem.getRelsExtIndex()));
				KramAvailability oldItem = kramAvailabilityDAO.getByConfigAndUuid(newItem.getHarvestedFrom(), newItem.getUuid());
				if (oldItem == null) { // new
					newItem.setUpdated(new Date());
					newItem.setLastHarvest(new Date());
					getPageValues(kramAvailabilityDAO, newItem);
					kramAvailabilityDAO.saveOrUpdate(newItem);
					continue;
				}
				if (isUpdated(oldItem, newItem)) {
					kramAvailabilityDAO.dropKeys(oldItem);
					oldItem.setParentUuid(newItem.getParentUuid());
					oldItem.setAvailability(newItem.getAvailability());
					oldItem.setDnnt(newItem.isDnnt());
					oldItem.setDnntLabels(newItem.getDnntLabels());
					oldItem.setUpdated(new Date());
					oldItem.setIssn(newItem.getIssn());
					oldItem.setIssue(newItem.getIssue());
					oldItem.setVolume(newItem.getVolume());
					oldItem.setYaer(newItem.getYaer());
					oldItem.setPage(newItem.getPage());
					oldItem.setRelsExtIndex(newItem.getRelsExtIndex());
					oldItem.setType(newItem.getType());
					oldItem.setLevel(newItem.getLevel());
					getPageValues(kramAvailabilityDAO, oldItem);
				}
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

	public static void getPageValues(KramAvailabilityDAO availabilityDAO, final KramAvailability availability) {
		if (!availability.getType().equals("page") && !availability.getType().equals("periodicalitem")) return;
		KramAvailability issue;
		if (availability.getType().equals("page")) {
			issue = availabilityDAO.getByConfigAndUuid(availability.getHarvestedFrom(), availability.getParentUuid());
			if (issue == null) return;
			availability.setIssue(issue.getIssue());
			availability.setIssn(issue.getIssn());
		} else issue = availability;
		KramAvailability volume = availabilityDAO.getByConfigAndUuid(availability.getHarvestedFrom(), issue.getParentUuid());
		if (volume == null) return;
		availability.setVolume(volume.getVolume());
		if (availability.getIssn() != null && !availability.getIssn().isEmpty() && availability.getYaer() != null && availability.getVolume() != null
				&& availability.getIssue() != null) {
			List<String> results = new ArrayList<>(Arrays.asList(availability.getIssn(),
					availability.getYaer().toString(), availability.getVolume().toString(), availability.getIssue().toString()));
			if (availability.getType().equals("periodicalitem")) {
				availability.setDedupKey(StringUtils.join(results, ';'));
			}
			if (availability.getPage() != null) {
				results.add(availability.getPage().toString());
				availability.setDedupKey(StringUtils.join(results, ";"));
			}
		}
	}

	private Integer parsePage(final Integer page, final Integer relsExtIndex) {
		if (relsExtIndex == null || page == null) return page;
		return relsExtIndex + 1 == page ? page : null;
	}

}
