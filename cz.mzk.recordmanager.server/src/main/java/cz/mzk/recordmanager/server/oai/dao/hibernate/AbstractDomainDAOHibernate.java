package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.oai.dao.DomainDAO;

public class AbstractDomainDAOHibernate<ID extends Serializable, T> implements DomainDAO<ID, T> {

	@Autowired
	protected SessionFactory sessionFactory;
	
	private final Class<T> persistenceClass = getClassForPersistence();
	
	@SuppressWarnings("unchecked")
	@Override
	public T get(ID id) {
		return (T) sessionFactory.getCurrentSession().get(persistenceClass, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T load(ID id) {
		return (T) sessionFactory.getCurrentSession().load(persistenceClass, id);
	}

	@Override
	public T persist(T entity) {
		sessionFactory.getCurrentSession().persist(entity);
		return entity;
	}

	@Override
	public void delete(T entity) {
		sessionFactory.getCurrentSession().delete(entity);
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getClassForPersistence() {
		Class<?> clazz = this.getClass();
		while (clazz.getSuperclass() != null 
			    && !clazz.getSuperclass().equals(AbstractDomainDAOHibernate.class)) {
			clazz = clazz.getSuperclass();
		}
		if (clazz.getSuperclass() == null) {
			throw new AssertionError(String.format("Class %s not instanceof AbstractDomainDAO!", this.getClass().getCanonicalName()));
		}
		return (Class<T>) ((ParameterizedType) 
				  clazz.getGenericSuperclass()).getActualTypeArguments()[1];
	}

	@Override
	public void flush() {
		sessionFactory.getCurrentSession().flush();
	}

}
