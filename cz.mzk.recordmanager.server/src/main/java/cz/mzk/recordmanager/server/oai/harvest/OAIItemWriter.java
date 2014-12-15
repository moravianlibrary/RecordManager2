package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Component
@Scope("step")
public class OAIItemWriter implements ItemWriter<List<OAIRecord>> {

	@Override
	public void write(List<? extends List<OAIRecord>> items) throws Exception {
		// FIXME
	}

}
