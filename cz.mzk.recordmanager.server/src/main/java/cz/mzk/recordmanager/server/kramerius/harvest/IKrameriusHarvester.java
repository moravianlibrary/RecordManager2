package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;

public interface IKrameriusHarvester {

	List<String> getNextUuids() throws SolrServerException;

	List<HarvestedRecord> getRecords(List<String> uuids) throws IOException;

	Integer getStart();

	void setStart(int start);

	String getNextPid();

	void setNextPid(String nextPid);
}
