package cz.mzk.recordmanager.server.springbatch;

import java.util.Date;
import java.util.Map;

import org.joda.time.Period;
import org.springframework.batch.item.ExecutionContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.OAIGranularity;

public class DateIntervalPartitionerTest extends AbstractTest {

	@Test
	public void test() {
		Date from = OAIGranularity.stringToDate("2010-06-30T01:20:00Z");
		Date to = OAIGranularity.stringToDate("2012-09-30T01:20");
		DateIntervalPartitioner interval = new DateIntervalPartitioner(from, to, Period.months(1), "to", "from");
		Map<String, ExecutionContext> intervals = interval.partition(0);
		Assert.assertEquals(intervals.size(), 28);
	}
	
}
