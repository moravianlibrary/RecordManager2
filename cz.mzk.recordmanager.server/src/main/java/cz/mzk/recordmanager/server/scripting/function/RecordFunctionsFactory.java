package cz.mzk.recordmanager.server.scripting.function;

import java.util.Collection;
import java.util.Map;

public interface RecordFunctionsFactory {

	public <T, S extends Object> Map<String, RecordFunction<T>> create(Class<T> clazz, Collection<S> functions);

}
