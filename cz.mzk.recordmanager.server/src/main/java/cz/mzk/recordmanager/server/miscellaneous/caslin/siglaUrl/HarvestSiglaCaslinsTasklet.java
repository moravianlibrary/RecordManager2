package cz.mzk.recordmanager.server.miscellaneous.caslin.siglaUrl;

import cz.mzk.recordmanager.server.model.SiglaCaslin;
import cz.mzk.recordmanager.server.oai.dao.SiglaCaslinDAO;
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


/**
 * Tasklet communicates with Aleph on NKP using X-services
 * and downloads identifiers of manually merged records from SKAT
 *
 * @author mertam
 */
public class HarvestSiglaCaslinsTasklet implements Tasklet {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SiglaCaslinDAO siglaCaslinDAO;

	private static final Logger logger = LoggerFactory.getLogger(HarvestSiglaCaslinsTasklet.class);

	private static final String URL = "http://aleph.nkp.cz/web/cpk/skc_links";

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		for (String line : harvest()) {
			if (line.trim().startsWith("*")) continue;
			String[] splitLine = line.split(";");
			if (splitLine.length == 4) {
				siglaCaslinDAO.saveOrUpdate(SiglaCaslin.create(splitLine[0].substring(0, 6), splitLine[3] + splitLine[2]));
			}
		}
		return RepeatStatus.FINISHED;
	}

	protected String[] harvest() throws RuntimeException {
		try (InputStream is = httpClient.executeGet(URL)) {
			logger.info("Downloading: " + URL);
			return IOUtils.toString(is, StandardCharsets.UTF_8).split("\n");
		} catch (IOException e) {
			logger.error("Could not download ziskej list");
			throw new RuntimeException();
		}
	}

}
