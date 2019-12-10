package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

/**
 * DedupArticlesTGProcessor subclass for processing records having source_info_x
 */
public class DedupArticlesTGProcessor extends
		DedupSimpleKeysStepProcessor {

	public DedupArticlesTGProcessor(boolean disadvantagedStep) {
		super(disadvantagedStep);
	}

	/**
	 * On the input are two {@link HarvestedRecord}s, having same source_info_x
	 */
	@Override
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		String source_info_xA = hrA.getSourceInfoX();
		String source_info_xB = hrB.getSourceInfoX();

		// return false if both records have source_info_x which are not same
		if (source_info_xA != null && source_info_xB != null) {
			if (!source_info_xA.equals(source_info_xB)) {
				return false;
			}
		}

		return super.matchRecords(hrA, hrB);
	}
}
