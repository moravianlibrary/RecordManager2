package cz.mzk.recordmanager.server.miscellaneous.ziskej;

import cz.mzk.recordmanager.server.model.ZiskejLibrary;
import cz.mzk.recordmanager.server.oai.dao.ZiskejLibraryDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Tasklet communicates with Aleph on NKP using X-services
 * and downloads identifiers of manually merged records from SKAT
 *
 * @author mertam
 */
public class HarvestZiskejLibrariesTasklet implements Tasklet {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ZiskejLibraryDAO ziskejLibraryDAO;

	private static final Logger logger = LoggerFactory.getLogger(HarvestZiskejLibrariesTasklet.class);

	private static final String URL = "https://ziskej.techlib.cz:9080/api/v1/libraries";

	@Override
	public RepeatStatus execute(StepContribution contribution,
	                            ChunkContext chunkContext) throws Exception {
		JSONObject obj = new JSONObject(harvest());

		List<String> list = new ArrayList<>();
		JSONArray jsonArray = obj.getJSONArray("items");
		if (jsonArray != null) {
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				list.add(jsonArray.get(i).toString());
			}
		}
		for (String sigla : list) {
			ZiskejLibrary lib = ziskejLibraryDAO.getBySigla(sigla);
			if (lib == null) {
				lib = new ZiskejLibrary();
				lib.setSigla(sigla);
				lib.setUpdated(new Date());
			}
			lib.setLastHarvest(new Date());
			ziskejLibraryDAO.saveOrUpdate(lib);
		}
		return RepeatStatus.FINISHED;
	}

	protected String harvest() throws RuntimeException {
		try (InputStream is = httpClient.executeGet(URL)) {
			logger.info("Downloading: " + URL);
			return IOUtils.toString(is);
		} catch (IOException e) {
			logger.error("Could not download ziskej list");
			throw new RuntimeException();
		}
	}

}
