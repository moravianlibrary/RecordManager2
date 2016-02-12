package cz.mzk.recordmanager.server.dedup.clustering;


/**
 * Class implementing this interface can be used for similarity clustering.
 *
 */
public interface Clusterable {

	
	/**
	 * get unique identifier of object.
	 * @return
	 */
	public Long getId();
	
	/**
	 * 
	 * @param other
	 * @return integer representation of percentage matching between object [0, 100] 
	 */
	public int computeSimilarityPercentage(Clusterable other);
	
	
}
