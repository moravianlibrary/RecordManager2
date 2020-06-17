package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.ZiskejLibrary;

public interface ZiskejLibraryDAO extends DomainDAO<Long, ZiskejLibrary> {

	ZiskejLibrary getBySigla(final String sigla);
}
