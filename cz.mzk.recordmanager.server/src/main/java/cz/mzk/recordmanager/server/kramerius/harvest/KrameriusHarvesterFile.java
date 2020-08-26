package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class KrameriusHarvesterFile extends KrameriusHarvesterImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(KrameriusHarvesterFile.class);

	BufferedReader buffer;

	public KrameriusHarvesterFile(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom, String inFile) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom, inFile);
		try {
			buffer = new BufferedReader(new FileReader(new File(inFile)));
		} catch (FileNotFoundException e) {
			LOGGER.error("File {} not found!", inFile);
		} catch (NullPointerException e) {
			LOGGER.error("Missing file name");
		}
	}

	@Override
	public List<String> getNextUuids() throws IOException {
		if (inFile == null) throw new FileNotFoundException();
		// get uuids
		List<String> uuids = new ArrayList<>();
		int i = 0;
		while (buffer.ready() && i < params.getQueryRows()) {
			uuids.add(buffer.readLine().trim());
			i++;
		}
		return uuids.isEmpty() ? null : uuids;
	}

}
