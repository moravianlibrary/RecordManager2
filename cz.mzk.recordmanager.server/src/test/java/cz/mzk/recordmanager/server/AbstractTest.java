package cz.mzk.recordmanager.server;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@ContextConfiguration(classes={AppConfigDev.class, AppConfig.class}, loader=AnnotationConfigContextLoader.class)
public class AbstractTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	protected DBUnitHelper dbUnitHelper;
	
	@BeforeMethod
	public void setUp() {
		Session session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
	}
	
	@AfterMethod
	public void tearDown() throws Exception {
	    SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
	    Session session = holder.getSession(); 
	    session.flush();
	    TransactionSynchronizationManager.unbindResource(sessionFactory);
	    SessionFactoryUtils.closeSession(session);
	}
	
}
