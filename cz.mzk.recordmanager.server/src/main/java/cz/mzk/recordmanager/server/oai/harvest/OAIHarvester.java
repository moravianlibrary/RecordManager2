package cz.mzk.recordmanager.server.oai.harvest;

import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;

public interface OAIHarvester {

	OAIListRecords listRecords(String resumptionToken);

	OAIListIdentifiers listIdentifiers(String resumptionToken);

	OAIGetRecord getRecord(String identifier);

	OAIIdentify identify();

}
