package cz.mzk.recordmanager.server.scripting.function;

public interface RecordFunction<T> {

	public Object apply(T record, Object args);

}
