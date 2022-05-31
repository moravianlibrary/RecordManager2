package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PalmknihyMetadataMarcRecord extends EbooksMetadataMarcRecord {

	@Autowired
	private ImportConfigurationDAO icDao;

	private static final String COMMENT = "Registrovaní uživatelé knihovny získají knihu po přihlášení ve svém katalogu";

	private static final Pattern PALMKNIHY_PATTERN = Pattern.compile("palmknihy.cz");

	public PalmknihyMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) {
				if (PALMKNIHY_PATTERN.matcher(df.getSubfield('u').getData()).find()
						|| df.getSubfield('z') == null) continue;
				results.add(MetadataUtils.generateUrl(df.getSubfield('z').getData(),
						Constants.DOCUMENT_AVAILABILITY_MEMBER, df.getSubfield('u').getData(), COMMENT));
			}
		}
		return results;
	}

	@Override
	public List<String> getCustomInstitutionFacet() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('z') == null) continue;
			List<ImportConfiguration> configs = icDao.findByIdPrefix(df.getSubfield('z').getData());
			if (configs.isEmpty()) continue;
			results.addAll(SolrUtils.getRegionInstitution(configs.get(0)));
		}
		return results;
	}

	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;
		return !underlayingMarc.getDataFields("856").isEmpty();
	}

	@Override
	public boolean matchFilterEbooks() {
		return true;
	}

}
