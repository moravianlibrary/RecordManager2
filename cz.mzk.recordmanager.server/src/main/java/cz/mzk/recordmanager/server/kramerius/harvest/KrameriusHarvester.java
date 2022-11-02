package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public interface KrameriusHarvester {

	List<String> getNextUuids() throws SolrServerException, IOException;

	List<HarvestedRecord> getRecords(List<String> uuids) throws IOException;

	JSONObject info(String url) throws IOException;

}
