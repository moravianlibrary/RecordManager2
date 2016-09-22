package cz.mzk.recordmanager.server.oai.harvest;

import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;

public interface OAIHarvester {

	public OAIListRecords listRecords(String resumptionToken);

	public OAIListIdentifiers listIdentifiers(String resumptionToken);

	public OAIGetRecord getRecord(String identifier);

	public OAIIdentify identify();

}
