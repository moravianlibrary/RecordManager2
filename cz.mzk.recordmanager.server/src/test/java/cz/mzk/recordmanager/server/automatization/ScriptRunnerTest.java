package cz.mzk.recordmanager.server.automatization;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

public class ScriptRunnerTest extends AbstractTest {

	@Autowired
	private ScriptRunner scriptRunner;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void run() {
		scriptRunner.run("Harvest.groovy");
	}

}
