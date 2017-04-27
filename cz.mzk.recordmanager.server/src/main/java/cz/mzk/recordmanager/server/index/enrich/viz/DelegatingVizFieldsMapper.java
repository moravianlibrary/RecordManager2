package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DelegatingVizFieldsMapper implements VizFieldsMapper,
		InitializingBean {

	@Autowired
	private List<VizFieldsMapper> vizMappers;

	private Map<String, VizFieldsMapper> mappersBySource = new HashMap<>();

	@Override
	public List<String> getSuppoprtedSources() {
		return new ArrayList<String>(mappersBySource.keySet());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (VizFieldsMapper vizMapper : vizMappers) {
			for (String source : vizMapper.getSuppoprtedSources()) {
				mappersBySource.put(source, vizMapper);
			}
		}
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = value.split("\\|");
		mappersBySource.get(split[0] + split[1]).parse(value, document);
	}

}
