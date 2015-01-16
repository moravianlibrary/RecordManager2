package cz.mzk.recordmanager.server;

import java.io.Closeable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;

@ContextConfiguration(classes={AppConfigDev.class, AppConfig.class}, loader=AnnotationConfigContextLoader.class)
public class AbstractTest extends AbstractTestNGSpringContextTests {

	@Autowired
	protected DBUnitHelper dbUnitHelper;
	
	@Autowired
	private HibernateSessionSynchronizer sync;
	
	private Closeable currentSession = null;
	
	@BeforeMethod
	public void setUp() {
		currentSession = sync.register();
	}
	
	@AfterMethod
	public void tearDown() throws Exception {
		if (currentSession != null) {
			currentSession.close();
		}
	}
	
}
