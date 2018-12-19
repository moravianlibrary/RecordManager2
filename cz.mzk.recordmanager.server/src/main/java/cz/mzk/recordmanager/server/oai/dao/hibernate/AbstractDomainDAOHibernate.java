package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
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

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll() {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(persistenceClass);
		return (List<T>) crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findByIds(List<ID> ids) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(persistenceClass);
		crit.add(Restrictions.in("id", ids));
		return (List<T>) crit.list();
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

	@Override
	public T saveOrUpdate(T object) {
		sessionFactory.getCurrentSession().saveOrUpdate(object);
		return object;
	}
}
