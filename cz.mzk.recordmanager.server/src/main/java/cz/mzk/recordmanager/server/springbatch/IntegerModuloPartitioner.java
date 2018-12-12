package cz.mzk.recordmanager.server.springbatch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class IntegerModuloPartitioner implements Partitioner {

	public final static String DEFAULT_KEY = "modulo";

	private final String key;

	public IntegerModuloPartitioner(String key) {
		this.key = key;
	}

	public IntegerModuloPartitioner() {
		this(DEFAULT_KEY);
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<String, ExecutionContext>(gridSize);
		for (int i = 0; i < gridSize; i++) {
			ExecutionContext partition = new ExecutionContext();
			partition.put(this.key, i);
			map.put(Integer.toString(i), partition);
		}
		return map;
	}

}
