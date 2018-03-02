package cz.mzk.recordmanager.server.scripting.function;

public interface RecordFunction<T> {

	Object apply(T record, Object args);

}
