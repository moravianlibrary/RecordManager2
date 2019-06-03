package cz.mzk.recordmanager.server.index.enrich.viz;

import com.google.common.collect.Lists;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DelegatingVizFieldsMapper implements VizFieldsMapper,
		InitializingBean {

	@Autowired
	private List<VizFieldsMapper> vizMappers;

	private Map<String, List<VizFieldsMapper>> mappersBySource = new HashMap<>();

	@Override
	public List<String> getSuppoprtedSources() {
		return new ArrayList<String>(mappersBySource.keySet());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (VizFieldsMapper vizMapper : vizMappers) {
			for (String source : vizMapper.getSuppoprtedSources()) {
				if (mappersBySource.containsKey(source)) {
					mappersBySource.get(source).add(vizMapper);
				} else mappersBySource.put(source, Lists.newArrayList(vizMapper));
			}
		}
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = value.split("\\|");
		for (VizFieldsMapper mapper : mappersBySource.get(split[0] + split[1])) {
			mapper.parse(value, document);
		}
	}

}
