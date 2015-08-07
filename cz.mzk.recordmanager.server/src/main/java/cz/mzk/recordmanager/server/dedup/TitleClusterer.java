package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.StringUtils;

public class TitleClusterer {
	
	private static Logger logger = LoggerFactory.getLogger(TitleClusterer.class);

	private final int TITLE_MATCH_PERCENTAGE = 70;
	
	private final int TITLE_PREFIX_BOUNDARY = 8;
	
	private final List<TitleForDeduplication> inputList;
	
	private Map<Long, Long> mapping = new ConcurrentHashMap<>();
	
	private Map<Long, Set<Long>> clusters = new ConcurrentHashMap<>();
	
	boolean isInitialized = false;
	
	private long startTime = 0L;
	/**
	 * @param inputList
	 */
	public TitleClusterer(List<TitleForDeduplication> inputList) {
		this.inputList = inputList;
	}
	
	public void initCluster() {
    	logger.info(String.format("Creating title cluster from %d records.", inputList.size()));
    	startTime = Calendar.getInstance().getTimeInMillis();
    	
    	createClusters(inputList, mapping, clusters);
    	
    	long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
    	logger.info(String.format("Title cluster created, building took %d seconds", elapsedSecs));
    	isInitialized = true;
	}
	
	public boolean isInialized() {
		return isInitialized;
	}
	
	/**
	 * return set of titles that are similar enough to given {@link Title}
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
    	
	public void createClusters(List<TitleForDeduplication> input,
			Map<Long, Long> mapping, Map<Long, Set<Long>> clusters) {
		for (int i = 0; i < input.size(); i++) {
			TitleForDeduplication currentTitle = input.get(i);
			for (int j = i + 1; j < input.size(); j++) {
				TitleForDeduplication tmpTitle = input.get(j);
				if (currentTitle.getTitle().equals(tmpTitle.getTitle())) {
					// ignore same titles, these are deduplicated in previous
					// steps
					continue;
				}
				if (currentTitle.getIsbn() != null
						&& tmpTitle.getIsbn() != null
						&& !currentTitle.getIsbn().equals(tmpTitle.getIsbn())) {
					// ignore different ISBNs
					continue;
				}
				if (currentTitle.getCnb() != null && tmpTitle.getCnb() != null
						&& !currentTitle.getCnb().equals(tmpTitle.getCnb())) {
					// ignore different CNBs
					continue;
				}
				if (currentTitle.getAuthorStr() != null
						&& tmpTitle.getAuthorStr() != null
						&& !currentTitle.getAuthorStr().equals(
								tmpTitle.getAuthorStr())) {
					// ignore different authors
					continue;
				}

				int match = StringUtils.simmilarTitleMatchPercentage(
						currentTitle.getTitle(), tmpTitle.getTitle(),
						TITLE_MATCH_PERCENTAGE, TITLE_PREFIX_BOUNDARY);
				if (match > TITLE_MATCH_PERCENTAGE) {
					writeResult(currentTitle.getId(), tmpTitle.getId());
				}
			}
		}

	}
		
	private void writeResult(Long id1, Long id2) {
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