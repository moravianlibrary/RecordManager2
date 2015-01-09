package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class DedupKeysGeneratorWriter implements ItemWriter<HarvestedRecord>  {

	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {
		
	}

}
