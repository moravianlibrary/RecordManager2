package cz.mzk.recordmanager.server.oai.dao.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordFormatDAO;

@Component
public class HarvestedRecordFormatDAOHibernate extends AbstractDomainDAOHibernate<Long, HarvestedRecordFormat>
		implements HarvestedRecordFormatDAO {
	
	@Override
	public List<HarvestedRecordFormat> getFormatsFromEnums(
			List<HarvestedRecordFormatEnum> enums) {
		Set<HarvestedRecordFormat> formatSet = new HashSet<>();
		for (HarvestedRecordFormatEnum hrfe: enums) {
			formatSet.add(get(hrfe.getNumValue()));
		}
		return new ArrayList<>(formatSet);
	}
	

}
