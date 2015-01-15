package cz.mzk.recordmanager.server.oai.dao;

import java.io.Serializable;

import org.springframework.stereotype.Repository;

@Repository
public interface DomainDAO<ID extends Serializable, T> {
	
	public T get(ID id);
	
	public T load(ID id);
	
	public T persist(T object);
	
	public void delete(T object);

}
