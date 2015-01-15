package cz.mzk.recordmanager.server.dedup;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;

public class DedupRecordLocatorTest extends AbstractTest {
	
	@Autowired
	private DedupRecordLocator dedupRecordLocator;
	
	@Autowired
	private DBUnitHelper dbUnitHelper;
	
	@Test
	public void locate() {
	}

}
