package cz.mzk.recordmanager.server.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class HibernateChunkListener implements ChunkListener {
	
	private static Logger logger = LoggerFactory.getLogger(HibernateChunkListener.class);

	@Autowired
	private HibernateSessionSynchronizer sync;

	private SessionBinder currentSession;

	@Override
	public void beforeChunk(ChunkContext context) {
		currentSession = sync.register();
	}

	@Override
	public void afterChunk(ChunkContext context) {
		close();
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		close();
	}
	
	public void close() {
		if (currentSession == null) {
			return;
		}
		currentSession.close();
		currentSession = null;
	}

}
