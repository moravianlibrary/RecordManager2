package cz.mzk.recordmanager.server.dedup;

import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;

public class SimilarTitleProcessor implements
		ItemProcessor<List<TitleForDeduplication>, List<Set<Long>>> {

	
	@Override
	public List<Set<Long>> process(List<TitleForDeduplication> titles)
			throws Exception {
		
		TitleClusterer clusterer = new TitleClusterer(titles);
		clusterer.initCluster();
		return clusterer.getClusters();

	}
}
