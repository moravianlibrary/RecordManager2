package cz.mzk.recordmanager.server.imports;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;

public class ManuscriptoriumFulltextWriter implements
		ItemWriter<HarvestedRecordUniqueId> {

	private static Logger logger = LoggerFactory
			.getLogger(ManuscriptoriumFulltextWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	protected SessionFactory sessionFactory;

	private static String FULLTEXT_URL = "http://dbase.aipberoun.cz/manu3/oai/?verb=GetRecord&metadataPrefix=tei&identifier=";

	private ManuscriptoriumFulltextXmlStreamReader reader;

	private ApacheHttpClient client;

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> items)
			throws Exception {
		for (HarvestedRecordUniqueId uniqueId : items) {
			HarvestedRecord hr = harvestedRecordDao.get(uniqueId);
			if (!hr.getFulltextKramerius().isEmpty()) continue;
			getNextFulltext(uniqueId.getRecordId());
			FulltextKramerius fk = new FulltextKramerius();
			String fulltext = reader.next();
			if (fulltext.isEmpty()) {
				logger.warn("Fulltext from " + FULLTEXT_URL
						+ uniqueId.getRecordId() + " is empty.");
			} else {
				fk.setFulltext(fulltext.getBytes());
				fk.setUuidPage(uniqueId.getRecordId());
				fk.setPage("1");
				fk.setOrder(1L);
				hr.setFulltextKramerius(Collections.singletonList(fk));
				hr.setUpdated(new Date());
				harvestedRecordDao.persist(hr);
			}
			client.close();
		}

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

	}

	protected void getNextFulltext(String recordId) throws InterruptedException {
		try {
			logger.info("Harvesting fulltext from " + FULLTEXT_URL + recordId);
			client = new ApacheHttpClient();
			reader = new ManuscriptoriumFulltextXmlStreamReader(
					client.executeGet(FULLTEXT_URL + recordId));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
