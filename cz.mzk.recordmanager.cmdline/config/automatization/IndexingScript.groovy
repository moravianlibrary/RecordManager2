import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.IndexingFacade;

public class IndexingScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.IndexingScript");

	@Autowired
	private IndexingFacade indexingFacade;

	@Override
	public void run() {
		indexingFacade.index();
	}

}
