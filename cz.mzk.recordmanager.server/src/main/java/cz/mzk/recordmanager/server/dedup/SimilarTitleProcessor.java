package cz.mzk.recordmanager.server.dedup;

import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;

import cz.mzk.recordmanager.server.dedup.clustering.Cluster;
import cz.mzk.recordmanager.server.dedup.clustering.Clusterable;

public class SimilarTitleProcessor<T extends Clusterable> implements
		ItemProcessor<List<T>, List<Set<Long>>> {

	
	@Override
	public List<Set<Long>> process(List<T> titles)
			throws Exception {
		
		Cluster<T>  clusterer = new Cluster<>(titles);
		clusterer.initCluster();
		return clusterer.getClusters();

	}
}
