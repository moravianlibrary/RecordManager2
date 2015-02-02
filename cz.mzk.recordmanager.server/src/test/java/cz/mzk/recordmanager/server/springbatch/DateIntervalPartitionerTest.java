package cz.mzk.recordmanager.server.springbatch;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.batch.item.ExecutionContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.springbatch.DateIntervalPartitioner;

public class DateIntervalPartitionerTest extends AbstractTest {

	@Test
	public void test() {
		DateTime from = DateTime.parse("2010-06-30T01:20");
		DateTime to = DateTime.parse("2012-09-30T01:20");
		DateIntervalPartitioner interval = new DateIntervalPartitioner(from, to, Period.months(1), "to", "from");
		Map<String, ExecutionContext> intervals = interval.partition(0);
		Assert.assertEquals(intervals.size(), 28);
	}
	
}
