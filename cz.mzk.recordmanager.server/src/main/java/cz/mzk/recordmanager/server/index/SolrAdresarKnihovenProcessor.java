package cz.mzk.recordmanager.server.index;

import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;

public class SolrAdresarKnihovenProcessor implements ItemProcessor<AdresarKnihoven, List<SolrInputDocument>> {

	private static Logger logger = LoggerFactory.getLogger(SolrAdresarKnihovenProcessor.class);

	@Autowired
	private SolrInputDocumentFactory factory;

	@Override
	public List<SolrInputDocument> process(AdresarKnihoven adrRecord) throws Exception {
		logger.debug("About to process adresar_knihoven with id={}", adrRecord.getId());
		try {
			List<SolrInputDocument> result = Collections.singletonList(factory.create(adrRecord));
			logger.debug("Processing of adresar_knihoven with id={} finished", adrRecord.getId());
			return result;
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing adresar_knihoven with id=%s", adrRecord.getId()), ex);
			return null;
		}
	}

}
