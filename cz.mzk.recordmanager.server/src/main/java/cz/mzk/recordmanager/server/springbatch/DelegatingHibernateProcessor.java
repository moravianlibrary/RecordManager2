package cz.mzk.recordmanager.server.springbatch;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DelegatingHibernateProcessor<I, O> implements ItemProcessor<I, O> {

	private final SessionFactory sessionFactory;

	private final ItemProcessor<I, O> delegate;

	public DelegatingHibernateProcessor(SessionFactory sessionFactory, ItemProcessor<I, O> delegate) {
		super();
		this.sessionFactory = sessionFactory;
		this.delegate = delegate;
	}

	@Override
	public O process(I item) throws Exception {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			TransactionSynchronizationManager.bindResource(sessionFactory,
					new SessionHolder(session));
			return delegate.process(item);
		} finally {
			SessionFactoryUtils.closeSession(session);
			TransactionSynchronizationManager.unbindResource(sessionFactory);
		}
	}

}
