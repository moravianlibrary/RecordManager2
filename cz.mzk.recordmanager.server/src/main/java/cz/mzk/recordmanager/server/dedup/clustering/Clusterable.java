package cz.mzk.recordmanager.server.dedup.clustering;

/**
 * Class implementing this interface can be used for similarity clustering.
 */
public interface Clusterable {

	/**
	 * get unique identifier of object.
	 *
	 * @return unique identifier
	 */
	Long getId();

	/**
	 * @param other {@link Clusterable}
	 * @return integer representation of percentage matching between object [0, 100]
	 */
	int computeSimilarityPercentage(Clusterable other);


}
