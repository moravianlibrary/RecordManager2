package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.util.StringUtils;


/**
 * SimpleKeysStepProcessor subclass for processing records having same identifier.
 *
 */
public class DedupIdentifierClustersProcessor extends DedupSimpleKeysStepProsessor {

	private static final int TITLE_MATCH_BOUNDARY = 70;
	
	private static final int TITLE_PREFIX_BOUNDARY = 8;
	
	
	/**
	 * On the input are two {@link HarvestedRecord}s, having same CNB and common format.
	 * 
	 * Records should match if:
	 *   their titles are similar enough (TITLE_MATCH_BOUNDARY)
	 *   their publication years don't differ
	 *   
	 */
	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		List<Title> aTitles = hrA.getTitles();
		List<Title> bTitles = hrB.getTitles();
		
		if (aTitles.isEmpty() || bTitles.isEmpty()) {
			return false;
		}
		
		boolean titlesMatching = false;
		for (Title aTitle: aTitles) {
			for (Title bTitle: bTitles) {
				titlesMatching |= StringUtils.simmilarTitleMatch(aTitle, bTitle, TITLE_MATCH_BOUNDARY, TITLE_PREFIX_BOUNDARY);
			}
		}
		
		if (!titlesMatching) {
			return false;
		}
		
		if (hrA.getPublicationYear() != null 
				&& !hrA.getPublicationYear().equals(hrB.getPublicationYear())) {
			return false;
		}
		
		return true;
	}
}
