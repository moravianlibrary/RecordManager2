import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.facade.MiscellaneousFacade;

public class GenerateSkatKeysScript implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.DailyScript");

	@Autowired
	private MiscellaneousFacade miscellaneousFacade;

	@Override
	public void run() {
		miscellaneousFacade.runGenerateSkatDedupKeys();
	}

}
