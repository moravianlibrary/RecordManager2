package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachingStopWordsResolver implements StopWordsResolver {

	private final List<StopWordsResolver> delegates;

	private final Map<String, Set<String>> stopWordsMap = new ConcurrentHashMap<String, Set<String>>(16, 0.9f, 1);
	
	public CachingStopWordsResolver(StopWordsResolver... delegates) {
		super();
		this.delegates = Arrays.asList(delegates);
	}

	@Override
	public Set<String> resolve(String file) throws IOException {
		Set<String> stopWords = stopWordsMap.get(file);
		if (stopWords != null) {
			return stopWords;
		}
		for (StopWordsResolver delegate : delegates) {
			try {
				stopWords = delegate.resolve(file);
				break;
			} catch (IllegalArgumentException iae) {
				continue;
			}
		}
		if (stopWords == null) {
			throw new IllegalArgumentException(String.format("File %s not found", file));
		}
		stopWordsMap.put(file, stopWords);
		return stopWords;
	}

}
