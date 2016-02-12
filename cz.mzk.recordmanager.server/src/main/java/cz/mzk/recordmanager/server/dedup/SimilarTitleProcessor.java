package cz.mzk.recordmanager.server.dedup;

import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;

import cz.mzk.recordmanager.server.dedup.clustering.Cluster;
import cz.mzk.recordmanager.server.dedup.clustering.Clusterable;

public class SimilarTitleProcessor<T extends Clusterable> implements
		ItemProcessor<List<T>, List<Set<Long>>> {

	
	private static final int SIMILARITY_BOUNDARY = 70;
	
	@Override
	public List<Set<Long>> process(List<T> titles)
			throws Exception {
		
		Cluster<T>  clusterer = new Cluster<>(titles,SIMILARITY_BOUNDARY);
		clusterer.initCluster();
		return clusterer.getClusters();

	}
}
