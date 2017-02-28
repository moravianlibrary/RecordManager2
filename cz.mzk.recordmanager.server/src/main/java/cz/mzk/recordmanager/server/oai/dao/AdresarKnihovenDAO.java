package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;

public interface AdresarKnihovenDAO extends DomainDAO<Long, AdresarKnihoven> {

	public AdresarKnihoven findByRecordId(String recordId);
	
}
