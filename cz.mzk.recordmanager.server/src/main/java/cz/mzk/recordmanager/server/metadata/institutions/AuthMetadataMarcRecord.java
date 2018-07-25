package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.Authority;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AuthMetadataMarcRecord extends MetadataMarcRecord {

	@Autowired
	protected SessionFactory sessionFactory;

	private static final String FILTER_682I_START = "Záhlaví";
	private static final String FILTER_682I_END = "bylo nahrazeno záhlavím";

	public AuthMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		for (String data : underlayingMarc.getFields("682", 'i')) {
			data = data.trim();
			if (data.startsWith(FILTER_682I_START) && data.endsWith(FILTER_682I_END)) {
				Session session = sessionFactory.getCurrentSession();
				Query update = session.createQuery("UPDATE HarvestedRecord SET updated = :updated WHERE id in " +
						"(SELECT harvestedRecordId FROM Authority WHERE authorityId = :auth_id)");
				update.setParameter("auth_id", underlayingMarc.getControlField("001"));
				update.setParameter("updated", new Date());
				update.executeUpdate();
				return false;
			}
		}
		if (underlayingMarc.getDataFields("100").isEmpty()) return false;
		for (DataField df : underlayingMarc.getDataFields("100")) {
			if (df.getSubfield('t') != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.singletonList(HarvestedRecordFormatEnum.OTHER_PERSON);
	}

	@Override
	public String getAuthorityId() {
		for (DataField df : underlayingMarc.getDataFields("100")) {
			Subfield sf = df.getSubfield('7');
			if (sf != null) return sf.getData();
		}
		return null;
	}

	@Override
	public List<String> getUrls() {
		List<String> results = super.getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE);
		for (String link : underlayingMarc.getFields("998", 'a')) {
			results.add(MetadataUtils.generateUrl(Constants.DOCUMENT_AVAILABILITY_ONLINE, link, ""));
		}
		return results;
	}

	@Override
	public List<Authority> getAllAuthorAuthKey() {
		return null;
	}
}
