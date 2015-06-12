package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.HarvestedRecord;


/**
 * ItemProcessor implementation for DedupTitleAuthStep
 *
 */
public class DedupTitleAuthItemProcessor extends DedupSimpleKeysStepProsessor {
	
	public static final int PAGES_MATCH_PERCENTAGE = 90;
	
	/**
	 * Records match if their count of pages doesn't differ more than 100 - PAGES_MATCH_PERCENTAGE %.
	 */
	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		if (hrA == null || hrA.getPages() == null || hrB == null || hrB.getPages() == null) {
			return false;
		}
		
		Long pagesA = hrA.getPages();
		Long pagesB = hrB.getPages();
		
		Long aDiff = pagesA * (PAGES_MATCH_PERCENTAGE / 100);
		Long bDiff = pagesB * (PAGES_MATCH_PERCENTAGE / 100);
		if ((pagesA >= pagesB - bDiff && pagesA <= pagesB + bDiff) 
				|| (pagesB >= pagesA - aDiff && pagesB <= pagesA + aDiff)) {
			return true;
		}
		return false;
	}
}


