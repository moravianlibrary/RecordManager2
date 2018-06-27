package cz.mzk.recordmanager.server.miscellaneous.caslin.view;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class UpdateCaslinRecordsViewWriter implements ItemWriter<HarvestedRecordUniqueId> {

	private static Logger logger = LoggerFactory.getLogger(UpdateCaslinRecordsViewWriter.class);

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private DedupRecordDAO drDao;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private ListResolver listResolver;

	private ProgressLogger progress = new ProgressLogger(logger, 10000);

	private Set<String> viewSiglaList;

	private String viewName;

	private static final String VIEW_FILE = "view/%s_caslin.txt";

	public UpdateCaslinRecordsViewWriter(String view) {
		this.viewName = view;
	}

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> items) throws Exception {
		if (viewSiglaList == null) {
			try {
				viewSiglaList = listResolver.resolve(String.format(VIEW_FILE, viewName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (HarvestedRecordUniqueId uniqueId : items) {
			try {
				progress.incrementAndLogProgress();
				HarvestedRecord hr = hrDao.get(uniqueId);
				MarcRecord marcRecord = marcXmlParser.parseRecord(hr);
				for (String sigla : marcRecord.getFields("996", 'e')) {
					if (!viewSiglaList.contains(sigla.trim())) continue;
					DedupRecord dr = hr.getDedupRecord();
					if (dr == null) break;
					dr.setUpdated(new Date());
					drDao.persist(dr);
					break;
				}
			} catch (Exception ex) {
				logger.error(String.format("Exception thrown in harvested_record with id=%s", uniqueId), ex);
			}
		}
	}
}
