package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.viz.DelegatingVizFieldsMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class VizFieldsHarvestedRecordEnricher implements
		HarvestedRecordEnricher {

	@Autowired
	private DelegatingVizFieldsMapper mapper;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		List<String> keys = document.getFieldValues(SolrFieldConstants.VIZ_DUMMY_FIELD)
				.stream().map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());

		mapper.parse(keys, document);
	}

}
