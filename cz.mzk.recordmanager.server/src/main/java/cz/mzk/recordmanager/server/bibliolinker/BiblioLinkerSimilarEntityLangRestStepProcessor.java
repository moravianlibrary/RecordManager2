package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generic implementation of of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarEntityLangRestStepProcessor extends BiblioLinkerSimilarSimpleStepProcessor {

	public BiblioLinkerSimilarEntityLangRestStepProcessor() {
	}

	public BiblioLinkerSimilarEntityLangRestStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
	}

	@Override
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		List<HarvestedRecord> toUpdate = super.process(biblioIdsList);
		toUpdate.forEach(hr -> hr.setNextBiblioLinkerSimilarFlag(false));
		return toUpdate;
	}

}
