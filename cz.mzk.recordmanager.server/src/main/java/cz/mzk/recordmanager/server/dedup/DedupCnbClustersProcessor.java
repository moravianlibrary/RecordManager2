package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;

public class DedupCnbClustersProcessor extends DedupSimpleKeysStepProsessor {

	private static final int TITLE_MATCH_BOUNDARY = 90;
	
	
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
				titlesMatching |= matchTitle(aTitle.getTitleStr(), bTitle.getTitleStr()); 
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
	
	protected boolean matchTitle(String aTitle, String bTitle) {
		if (aTitle == null || aTitle.isEmpty() || bTitle == null || bTitle.isEmpty()) {
			return false; 
		}
		
		if (aTitle.startsWith(bTitle) || bTitle.startsWith(aTitle)) {
			return true;
		}
		
		if (levensteinTitleMatchPercentage(aTitle, bTitle) >= TITLE_MATCH_BOUNDARY) {
			return true;
		}
		
		return false;
	}
	
	protected int levensteinTitleMatchPercentage(final String origTitle,
			final String dedupTitle) {
		if (origTitle == null || dedupTitle == null
				|| (origTitle.isEmpty() && dedupTitle.isEmpty())) {
			return 0;
		}
		
		//don't compute Levenshtein if there is no chance to match due to length
		if (Math.min(origTitle.length(), dedupTitle.length()) < 
				Math.max(origTitle.length(), dedupTitle.length()) * (TITLE_MATCH_BOUNDARY / 100)) {
			return 0;
		}
		int dist = StringUtils.getLevenshteinDistance(origTitle, dedupTitle);
		float percentage = ((float) dist) / Math.min(origTitle.length(), dedupTitle.length());
		return (int) ((1 - percentage) * 100);
    }
}
