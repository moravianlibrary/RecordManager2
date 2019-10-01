package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MatchAllDataFieldMatcher;
import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.Authority;
import cz.mzk.recordmanager.server.model.BLTopicKey;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class AuthMetadataMarcRecord extends MetadataMarcRecord {

	@Autowired
	protected SessionFactory sessionFactory;

	private static final Pattern FILTER_682 = Pattern.compile("bylo nahrazeno z[aá]hlav[ií]m", Pattern.CASE_INSENSITIVE);
	private static final Pattern FILTER_682_DUPLICITA = Pattern.compile("duplicita", Pattern.CASE_INSENSITIVE);

	public AuthMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		for (DataField df : underlayingMarc.getDataFields("682")) {
			if (FILTER_682.matcher(df.toString()).find() || FILTER_682_DUPLICITA.matcher(df.toString()).find()) {
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

	@Override
	public List<BLTopicKey> getBiblioLinkerTopicKey() {
		List<BLTopicKey> result = new ArrayList<>();
		List<String> values = underlayingMarc.getFields("374", MatchAllDataFieldMatcher.INSTANCE,
				SubfieldExtractionMethod.SEPARATED, "", 'a');
		if (values.isEmpty()) values = underlayingMarc.getFields("372", MatchAllDataFieldMatcher.INSTANCE,
				SubfieldExtractionMethod.SEPARATED, "", 'a');
		if (values.isEmpty()) values = underlayingMarc.getFields("370", MatchAllDataFieldMatcher.INSTANCE,
				SubfieldExtractionMethod.SEPARATED, "", 'c');
		for (String value : values) {
			result.add(BLTopicKey.create(value));
		}
		return result;
	}
}
