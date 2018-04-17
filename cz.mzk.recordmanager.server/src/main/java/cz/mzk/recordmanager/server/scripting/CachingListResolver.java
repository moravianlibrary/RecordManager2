package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachingListResolver implements ListResolver {

	private final ListResolver delegate;

	private final Map<String, Set<String>> listsMap = new ConcurrentHashMap<>(16, 0.9f, 1);

	public CachingListResolver(ListResolver delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Set<String> resolve(String file) throws IOException {
		Set<String> list = listsMap.get(file);
		if (list != null) return list;
		list = delegate.resolve(file);
		if (list == null) {
			throw new IllegalArgumentException(String.format("File %s not found", file));
		}
		listsMap.put(file, list);
		return list;
	}

}
