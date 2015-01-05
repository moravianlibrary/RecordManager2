package cz.mzk.recordmanager.server.oai.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AppConfig;
import cz.mzk.recordmanager.server.AppConfigDev;
import cz.mzk.recordmanager.server.model.Library;

@ContextConfiguration(classes={AppConfigDev.class, AppConfig.class}, loader=AnnotationConfigContextLoader.class)
public class LibraryDAOTest extends AbstractTransactionalTestNGSpringContextTests {
	
	@Autowired
	private LibraryDAO libraryDao;
	
	@Test
	public void test() {
		Library library1 = new Library();
		library1.setName("MZK");
		libraryDao.persist(library1);
		Library library2 = libraryDao.get(library1.getId());
		Assert.assertEquals(library2.getId(), library1.getId());
	}

}
