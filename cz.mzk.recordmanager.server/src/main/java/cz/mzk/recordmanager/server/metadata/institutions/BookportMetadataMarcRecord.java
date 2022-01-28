package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.List;

public class BookportMetadataMarcRecord extends EbooksMetadataMarcRecord {

	private static final String COMMENT = "Registrovaní uživatelé knihovny získají knihu po přihlášení přes eduID.cz na Bookportu";

	public BookportMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) {
				results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getLibrary().getName().toLowerCase(),
						Constants.DOCUMENT_AVAILABILITY_MEMBER, df.getSubfield('u').getData(), COMMENT));
			}
		}
		return results;
	}

	@Override
	public List<String> getCustomInstitutionFacet() {
		return SolrUtils.createHierarchicFacetValues(SolrUtils.INSTITUTION_OTHERS, "ebook", Constants.PREFIX_BOOKPORT.toUpperCase());
	}

}
