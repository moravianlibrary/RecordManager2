package cz.mzk.recordmanager.server.util;

import java.io.Closeable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class HibernateSessionSynchronizer {

	@Autowired
	private SessionFactory sessionFactory;

	public static class SessionBinder implements Closeable {

		private SessionFactory sessionFactory;

		private Session session;

		public SessionBinder(SessionFactory sessionFactory) {
			this.sessionFactory = sessionFactory;
			SessionHolder holder = (SessionHolder) TransactionSynchronizationManager
					.getResource(sessionFactory);
			if (holder != null) {
				return;
			}
			this.session = sessionFactory.openSession();
			TransactionSynchronizationManager.bindResource(sessionFactory,
					new SessionHolder(session));
		}

		@Override
		public void close() {
			if (session == null) {
				return;
			}
			SessionHolder holder = (SessionHolder) TransactionSynchronizationManager
					.getResource(sessionFactory);
			Session currentSession = holder.getSession();
			if (currentSession == session) {
				currentSession.flush();
				TransactionSynchronizationManager
						.unbindResource(sessionFactory);
				SessionFactoryUtils.closeSession(currentSession);
			}
		}
	}

	public SessionBinder register() {
		return new SessionBinder(sessionFactory);
	}

}
