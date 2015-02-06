package cz.mzk.recordmanager.server.springbatch;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import cz.mzk.recordmanager.server.util.Constants;

public class DateIntervalPartitioner implements Partitioner {

	private final DateTime from;
	
	private final DateTime to;
	
	private final Period period;
	
	private final String toKey;
	
	private final String fromKey;
	
	private final Long YEAR_2000 = 946684800000L;
	
	public DateIntervalPartitioner(Date fromDate, Date toDate, Period period, String toKey, String fromKey) {
		super();
		this.from = fromDate == null ? new DateTime(YEAR_2000) : new DateTime(
				fromDate.getTime());
		this.to = toDate == null ? new DateTime(Calendar.getInstance()
				.getTimeInMillis()) : new DateTime(toDate.getTime());
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
			partition.put(fromKey, nextFrom.toDate());
			partition.put(toKey, nextTo.toDate());
			partitions.put(Integer.toString(index), partition);
			nextFrom = nextTo;
			index++;
		}
		return partitions;
		
	}
}
