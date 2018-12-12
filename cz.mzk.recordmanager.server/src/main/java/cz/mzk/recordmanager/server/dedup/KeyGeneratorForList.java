package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.item.KeyGenerator;

public enum KeyGeneratorForList implements KeyGenerator {

	INSTANCE;

	private static Logger logger = LoggerFactory.getLogger(DedupSimpleKeysStepProcessor.class);

	@Override
	public Object getKey(Object item) {
		if (item instanceof List<?>) {
			return new ArrayList<Object>((List<?>) item);
		}
		logger.warn("Item is not instanceof List, got: %s", item.getClass().getCanonicalName());
		return item;
	}

}
