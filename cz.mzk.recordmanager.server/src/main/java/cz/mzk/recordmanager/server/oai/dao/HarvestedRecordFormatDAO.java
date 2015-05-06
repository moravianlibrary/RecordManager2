package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public interface HarvestedRecordFormatDAO extends DomainDAO<Long, HarvestedRecordFormat	> {

	public List<HarvestedRecordFormat> getFormatsFromEnums(List<HarvestedRecordFormatEnum> enums);
}
