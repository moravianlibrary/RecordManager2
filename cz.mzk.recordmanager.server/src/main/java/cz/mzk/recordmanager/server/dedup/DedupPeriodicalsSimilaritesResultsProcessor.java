package cz.mzk.recordmanager.server.dedup;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Issn;

/**
 * ItemProcessor implementation for DedupPeriodicalsSimilaritesResultsStep
 */
public class DedupPeriodicalsSimilaritesResultsProcessor extends DedupSimpleKeysStepProcessor {

	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {

		if (hrA == null || hrB == null) return false;

		// return false if both records have at least one ISSN but intersection is empty
		List<Issn> hraIssn = hrA.getIssns();
		List<Issn> hrbIssn = hrB.getIssns();
		if (!hraIssn.isEmpty() && !hrbIssn.isEmpty()) {
			Set<String> hraIssnSet = hraIssn.stream().map(i -> i.getIssn()).collect(Collectors.toSet());
			if (hrbIssn.stream().filter(p -> hraIssnSet.contains(p.getIssn())).count() == 0) {
				return false;
			}
		}
		return true;
	}
}
