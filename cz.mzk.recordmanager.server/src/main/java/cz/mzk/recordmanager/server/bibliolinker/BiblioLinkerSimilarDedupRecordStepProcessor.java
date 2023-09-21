package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generic implementation of  ItemProcessor
 */
@Component
public class BiblioLinkerSimilarDedupRecordStepProcessor extends BiblioLinkerSimilarSimpleStepProcessor
		implements ItemProcessor<List<Long>, List<HarvestedRecord>> {

	public BiblioLinkerSimilarDedupRecordStepProcessor() {
	}

	public BiblioLinkerSimilarDedupRecordStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
		this.MAX_SIMILARS_FOR_STEP = Integer.MAX_VALUE;
		this.NEW_SIMILARS_FOR_STEP = Integer.MAX_VALUE;
	}

	public BiblioLinkerSimilarDedupRecordStepProcessor(BiblioLinkerSimilarType type, int similarityStepLimit, int newSimilarsCount) {
		this.type = type;
		this.MAX_SIMILARS_FOR_STEP = similarityStepLimit;
		this.NEW_SIMILARS_FOR_STEP = newSimilarsCount;
	}

	@Override
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		return processRecords(sortRecords(harvestedRecordDao.getByDedupRecordIds(biblioIdsList)));
	}

	@Override
	protected Long getIdForSorting(HarvestedRecord hr) {
		return hr.getDedupRecord().getId();
	}
}
