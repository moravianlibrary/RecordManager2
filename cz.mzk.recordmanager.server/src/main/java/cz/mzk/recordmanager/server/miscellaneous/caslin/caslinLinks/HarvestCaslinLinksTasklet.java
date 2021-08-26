package cz.mzk.recordmanager.server.miscellaneous.caslin.caslinLinks;

import cz.mzk.recordmanager.server.model.CaslinLinks;
import cz.mzk.recordmanager.server.oai.dao.CaslinLinksDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * Tasklet downloads links from CASLIN
 */
public class HarvestCaslinLinksTasklet implements Tasklet {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private CaslinLinksDAO caslinLinksDAO;

	private static final Logger logger = LoggerFactory.getLogger(HarvestCaslinLinksTasklet.class);

	private static final String URL = "http://aleph.nkp.cz/web/cpk/skc_links";

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		for (String line : harvest()) {
			if (line.trim().startsWith("*") || (line.length() > 6 && line.charAt(6) != '=')) continue;
			String[] splitLine = line.split(";");
			if (splitLine.length == 4) {
				String sigla = splitLine[0].substring(0, 6);
				String url = splitLine[3] + splitLine[2];
				CaslinLinks caslinLinks = caslinLinksDAO.getBySigla(sigla);
				if (caslinLinks == null) {
					caslinLinks = CaslinLinks.create(sigla, url);
				} else {
					if (!caslinLinks.getUrl().equals(url)) {
						caslinLinks.setUrl(url);
						caslinLinks.setUpdated(new Date());
					}
					caslinLinks.setLastHarvest(new Date());
				}
				caslinLinksDAO.saveOrUpdate(caslinLinks);
			}
		}
		return RepeatStatus.FINISHED;
	}

	protected String[] harvest() throws RuntimeException {
		try (InputStream is = httpClient.executeGet(URL)) {
			logger.info("Downloading: " + URL);
			return IOUtils.toString(is, StandardCharsets.UTF_8).split("\n");
		} catch (IOException e) {
			logger.error("Could not download caslin links list");
			throw new RuntimeException();
		}
	}

}
