package cz.mzk.recordmanager.server.kramerius.fulltext;

import cz.mzk.recordmanager.server.model.FulltextKramerius;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;

public interface KrameriusFulltexter {

	List<FulltextKramerius> getFulltextObjects(String rootUuid) throws IOException, SolrServerException;

	List<FulltextKramerius> getFulltextForRoot(String rootUuid) throws IOException, SolrServerException;

}
