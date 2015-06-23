package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Isbn;


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
		if (!(pagesA >= pagesB - bDiff && pagesA <= pagesB + bDiff) 
				&& !(pagesB >= pagesA - aDiff && pagesB <= pagesA + aDiff)) {
			return false;
		}
		
		//return false if there is at least one different CNB
		for (Cnb aCnb: hrA.getCnb()) {
			for (Cnb bCnb: hrB.getCnb()) {
				if (aCnb.getCnb() != null && bCnb.getCnb() != null 
						&& !aCnb.getCnb().equals(bCnb.getCnb())) {
					return false;
				}
			}
		}
		
		//return false if there is at least one different ISBN
		for (Isbn aIsbn: hrA.getIsbns()) {
			for (Isbn bIsbn: hrB.getIsbns()) {
				if (aIsbn.getIsbn() != null && bIsbn.getIsbn() != null 
						&& !aIsbn.getIsbn().equals(bIsbn.getIsbn())) {
					return false;
				}
			}
		}
		return true;
	}
}


