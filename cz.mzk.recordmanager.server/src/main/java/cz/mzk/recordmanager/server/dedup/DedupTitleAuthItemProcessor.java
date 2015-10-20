package cz.mzk.recordmanager.server.dedup;

import java.util.HashSet;
import java.util.Set;

import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.util.DeduplicationUtils;


/**
 * ItemProcessor implementation for DedupTitleAuthStep
 *
 */
public class DedupTitleAuthItemProcessor extends DedupSimpleKeysStepProcessor {
	
	public static final int PAGES_MATCH_PERCENTAGE = 90;
	
	/**
	 * Records match if their count of pages doesn't differ more than 100 - PAGES_MATCH_PERCENTAGE %.
	 */
	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		if (hrA == null || hrA.getPages() == null || hrB == null || hrB.getPages() == null) {
			return false;
		}
		
		Set<String> aTitles = new HashSet<>();
		Set<String> bTitles = new HashSet<>();		
		hrA.getTitles().stream().filter(t -> t.getTitleStr() != null && !t.getTitleStr().isEmpty()).forEach(t -> aTitles.add(t.getTitleStr()));
		hrB.getTitles().stream().filter(t -> t.getTitleStr() != null && !t.getTitleStr().isEmpty()).forEach(t -> bTitles.add(t.getTitleStr()));
		Set<String> intersection = new HashSet<>(aTitles);
		intersection.retainAll(bTitles);
		
		
		if (intersection.stream().allMatch(s -> s.length() < 16)) {
			return false;
		}
		
		Long pagesA = hrA.getPages();
		Long pagesB = hrB.getPages();
		
		if (!DeduplicationUtils.comparePages(pagesA, pagesB, .05, 10)) {
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


