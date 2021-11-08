import cz.mzk.recordmanager.server.facade.MiscellaneousFacade
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

public class GenerateTopResults implements Runnable {

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.automatization.GenerateTopResults");

	@Autowired
	private MiscellaneousFacade miscellaneousFacade;

	@Override
	public void run() {
		try {
			miscellaneousFacade.runGenerateTopResults();
		} catch (JobExecutionFailure jfe) {
			logger.error(String.format("Top results failed"), jfe);
		}
	}

}
