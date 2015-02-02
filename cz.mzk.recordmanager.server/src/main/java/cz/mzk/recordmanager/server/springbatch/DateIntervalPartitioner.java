package cz.mzk.recordmanager.server.springbatch;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class DateIntervalPartitioner implements Partitioner {

	private final DateTime from;
	
	private final DateTime to;
	
	private final Period period;
	
	private final String toKey;
	
	private final String fromKey;
	
	public DateIntervalPartitioner(DateTime from, DateTime to, Period period, String toKey, String fromKey) {
		super();
		this.from = from;
		this.to = to;
		this.period = period;
		this.toKey = toKey;
		this.fromKey = fromKey;
	}
	
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> partitions = new LinkedHashMap<String, ExecutionContext>();
		int index = 1;
		DateTime nextFrom = from;
		DateTime nextTo = to;
		while (nextFrom.isBefore(to)) {
			nextTo = nextFrom.plus(period);
			if (nextTo.isAfter(to)) {
				nextTo = to;
			}
			ExecutionContext partition = new ExecutionContext();
			partition.put(fromKey, nextFrom);
			partition.put(toKey, nextTo);
			partitions.put(Integer.toString(index), partition);
			nextFrom = nextTo;
			index++;
		}
		return partitions;
	}

}
