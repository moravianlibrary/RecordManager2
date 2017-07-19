package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

/**
 * DedupIdentifierCNBClustersProcessor subclass for processing records having
 * same CNB
 *
 */
public class DedupIdentifierCNBClustersProcessor extends
		DedupIdentifierClustersProcessor {

	/**
	 * On the input are two {@link HarvestedRecord}s, having same identifier CNB
	 * and common format.
	 * 
	 * Records should match if their titles are similar enough
	 * (TITLE_MATCH_BOUNDARY)
	 * 
	 */
	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		Long yearA = hrA.getPublicationYear();
		Long yearB = hrB.getPublicationYear();

		// return false if both records have publication year which are not same
		if (yearA != null && yearB != null) {
			if (!yearA.equals(yearB)) {
				return false;
			}
		}

		return super.matchRecords(hrA, hrB);
	}
}
