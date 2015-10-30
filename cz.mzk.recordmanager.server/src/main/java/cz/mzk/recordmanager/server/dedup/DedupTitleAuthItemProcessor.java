package cz.mzk.recordmanager.server.dedup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		
//		return false if both records have at least one CNB but intersection is empty
		List<Cnb> hraCnbs = hrA.getCnb();
		List<Cnb> hrbCnbs = hrB.getCnb();
		if (!hraCnbs.isEmpty() && !hrbCnbs.isEmpty()) {
			Set<String> hraCnbSet = hraCnbs.stream().map(i -> i.getCnb()).collect(Collectors.toSet());
			if (hrbCnbs.stream().map(i -> i.getCnb()).filter(p -> hraCnbSet.contains(p)).count() == 0) {
				return false;
			} 
		}
		
//		return false if both records have at least one ISBN but intersection is empty		
		List<Isbn> hraIsbn = hrA.getIsbns();
		List<Isbn> hrbIsbn = hrB.getIsbns();
		if (!hraIsbn.isEmpty() && !hrbIsbn.isEmpty()) {
			Set<Long> hraIsbnSet = hraIsbn.stream().map(i -> i.getIsbn()).collect(Collectors.toSet());
			if (hrbIsbn.stream().mapToLong(i -> i.getIsbn()).filter(p -> hraIsbnSet.contains(p)).count() == 0) {
				return false;
			}	
		}
		
		return true;
	}
}


