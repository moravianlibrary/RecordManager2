package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.Collections;
import java.util.Set;

public class BiblioLinkerSimilarAuthorTitleStepProcessor extends BiblioLinkerSimilarSimpleStepProcessor {

	public BiblioLinkerSimilarAuthorTitleStepProcessor(BiblioLinkerSimilarType type) {
		super(type);
	}

	public BiblioLinkerSimilarAuthorTitleStepProcessor(BiblioLinkerSimilarType type, int similarityStepLimit, int newSimilarsCount) {
		super(type, similarityStepLimit, newSimilarsCount);
	}
	protected boolean isSimilar(HarvestedRecord hr1, HarvestedRecord hr2, Set<HarvestedRecord> similars) {
		if (!Collections.disjoint(hr1.getPhysicalFormats(), hr2.getPhysicalFormats())) return false;
		for (HarvestedRecord similar : similars) {
			if (!Collections.disjoint(hr2.getPhysicalFormats(), similar.getPhysicalFormats())) return false;
		}
		return true;
	}

}
