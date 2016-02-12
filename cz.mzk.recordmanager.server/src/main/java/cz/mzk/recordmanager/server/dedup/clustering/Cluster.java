package cz.mzk.recordmanager.server.dedup.clustering;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of similarity clustering.
 * 
 * Aim of this class is divide input objects into sets based on 
 * provided similarity metric.
 * 
 * Note that relation 'to be similar' is not transitive.
 * Computation is performed in quadratic time.
 *
 * @param <T>
 */
public class Cluster<T extends Clusterable>  {
	
	private static Logger logger = LoggerFactory.getLogger(Cluster.class);
	
	private final int matchThreshold;
	
	private final List<T> inputList;
	
	private Map<Long, Long> mapping = new HashMap<>();
	
	private Map<Long, Set<Long>> clusters = new HashMap<>();
	
	boolean isInitialized = false;
	
	private long startTime = 0L;

	
	/**
	 * Create new instance of similarity cluster.
	 * 
	 * @param inputList list of objects to be clustered
	 * @param matchThreshold percentage threshold for similarity matching
	 */
	public Cluster(List<T> inputList, int matchThreshold) {
		if (matchThreshold < 0 || matchThreshold > 100) {
			// assign default match threshold
			this.matchThreshold = 70;
			logger.warn("Using default match threshold for simmilarity clustering");
		} else {
			this.matchThreshold = matchThreshold;
		}
		
		this.inputList = inputList;
	}
	
	/**
	 * initialize clusters
	 */
	public void initCluster() {
    	logger.info(String.format("Creating cluster from %d records.", inputList.size()));
    	startTime = Calendar.getInstance().getTimeInMillis();
    	
    	createClusters(inputList, mapping, clusters);
    	
    	long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
    	logger.info(String.format("Cluster created, building took %d seconds", elapsedSecs));
    	isInitialized = true;
	}

	/**
	 * Return true if cluster is initialized (i.e. computations on input data were finished)
	 * @return
	 */
	public boolean isInialized() {
		return isInitialized;
	}
	
	/**
	 * return set of titles that are similar enough to given {@link Clusterable}
	 * @param title
	 * @return
	 */
	public Set<Long> getSimilarTitles(Long titleId) {
		if (!isInitialized) {
			return Collections.emptySet();
		}
		Long tid = mapping.get(titleId);
		if (tid == null) {
			return Collections.emptySet();
		}
		return clusters.get(tid);
	}
	
	/**
	 * return List of sets, each sets contains identifiers of similar records
	 * @return
	 */
	public List<Set<Long>> getClusters() {
		if (!isInitialized) {
			return Collections.emptyList();
		}
		return new ArrayList<Set<Long>>(clusters.values());
	}
    	

	protected void createClusters(List<T> input,
			Map<Long, Long> mapping, Map<Long, Set<Long>> clusters) {
		for (int i = 0; i < input.size(); i++) {
			T currentT = input.get(i);
			for (int j = i + 1; j < input.size(); j++) {
				T tmpT = input.get(j);
				if (currentT.computeSimilarityPercentage(tmpT) > matchThreshold) {
					writeResult(currentT.getId(), tmpT.getId());
				}
			}
		}

	}
		
	protected final void writeResult(Long id1, Long id2) {
		Long gid1 = mapping.get(id1);
		Long gid2 = mapping.get(id2);
		// two new clusters
		if (gid1 == null && gid2 == null) {
			Long idVal = TempIdSingleton.VALUE.nextVal();
			mapping.put(id1, idVal);
			mapping.put(id2, idVal);
			Set<Long> clusterSet = Collections
					.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
			clusterSet.add(id1);
			clusterSet.add(id2);
			clusters.put(idVal, clusterSet);
			return;
		}
		// add first to cluster of second
		if (gid1 == null && gid2 != null) {
			mapping.put(id1, gid2);
			clusters.get(gid2).add(id1);
			return;
		}
		// add second to cluster of first
		if (gid1 != null && gid2 == null) {
			mapping.put(id2, gid1);
			clusters.get(gid1).add(id2);
			return;
		}
		// merge clusters of both if we got this far
		for (Long currentKey : clusters.get(gid2)) {
			mapping.put(currentKey, gid1);
			clusters.get(gid1).add(currentKey);
		}
		// remove old cluster if necessary
		if (gid1 != gid2) {
			clusters.remove(gid2);
		}
	}
	
    protected enum TempIdSingleton {
    	VALUE;
  
    	private Long value = 0L;
    	
    	public synchronized Long nextVal() {
    		return ++value;
    	}
    }
}
