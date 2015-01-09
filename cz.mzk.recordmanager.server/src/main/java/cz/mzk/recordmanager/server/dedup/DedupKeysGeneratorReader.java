package cz.mzk.recordmanager.server.dedup;

import org.springframework.batch.item.ItemReader;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class DedupKeysGeneratorReader implements ItemReader<HarvestedRecord> {

	@Override
	public HarvestedRecord read() {
		return null;
	}

}
