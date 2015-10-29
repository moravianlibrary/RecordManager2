package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachingStopWordsResolver implements StopWordsResolver {

	private final StopWordsResolver delegate;

	private final Map<String, Set<String>> stopWordsMap = new ConcurrentHashMap<String, Set<String>>(16, 0.9f, 1);

	public CachingStopWordsResolver(StopWordsResolver delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Set<String> resolve(String file) throws IOException {
		Set<String> stopWords = stopWordsMap.get(file);
		if (stopWords != null) {
			return stopWords;
		}
		stopWords = delegate.resolve(file);
		if (stopWords == null) {
			throw new IllegalArgumentException(String.format("File %s not found", file));
		}
		stopWordsMap.put(file, stopWords);
		return stopWords;
	}

}
