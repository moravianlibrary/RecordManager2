package cz.mzk.recordmanager.server.oai.dao;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface DomainDAO<ID extends Serializable, T> {

	T get(ID id);

	T load(ID id);

	List<T> findAll();

	List<T> findByIds(List<ID> ids);

	T persist(T object);

	void delete(T object);

	void flush();

}
