package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;

import java.util.List;

public interface KrameriusConfigurationDAO extends DomainDAO<Long, KrameriusConfiguration> {

	List<KrameriusConfiguration> getAllWithoutOaiConfigs();

	List<Long> getAllDedupConfigIds();

}
