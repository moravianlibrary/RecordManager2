package cz.mzk.recordmanager.server.oai.dao;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface DomainDAO<ID extends Serializable, T> {

	public T get(ID id);

	public T load(ID id);

	public List<T> findAll();

	public T persist(T object);

	public void delete(T object);

	public void flush();

}
