package cz.mzk.recordmanager.server.dedup.clustering;


/**
 * {@link TitleClusterable} implementation suitable for periodicals.
 * 
 * TODO
 *
 */
public class PeriodicalTitleClusterable extends TitleClusterable {

	
	@Override
	public int computeSimilarityPercentage(Clusterable other) {
	
		if (other == null || !(other instanceof PeriodicalTitleClusterable)) {
			return 0;
		}
		PeriodicalTitleClusterable otherPeriodical = (PeriodicalTitleClusterable) other;
		
		// TODO 
		
		return super.computeSimilarityPercentage(other);
		
	}
}
