package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookportMetadataMarcRecord extends EbooksMetadataMarcRecord {

	private static final String COMMENT = "Registrovaní uživatelé knihovny získají knihu po přihlášení přes eduID.cz na Bookportu";

	private static final Pattern URL_PATTERN = Pattern.compile("(.*)(/kniha/.*)");

	private static final Map<String, String> URL_MAP = new HashMap<>();

	static {
		URL_MAP.put(Constants.PREFIX_SVKUL, "/AccountSaml/SignIn/?idp=https%3A%2F%2Fsvkul.cz%2Fidp%2Fshibboleth&returnUrl=");
		URL_MAP.put(Constants.PREFIX_NLK, "/AccountSaml/SignIn/?idp=https%3A%2F%2Fshib.medvik.cz%2Fidp%2Fshibboleth&returnUrl=");
		URL_MAP.put(Constants.PREFIX_KNAV, "/AccountSaml/SignIn/?idp=https%3A%2F%2Fidp.lib.cas.cz%2Fidp%2Fshibboleth&returnUrl=");
		URL_MAP.put(Constants.PREFIX_SVKHK, "/AccountSaml/SignIn/?idp=https%3A%2F%2Faleph.svkhk.cz%2Fidp%2Fshibboleth&returnUrl=");
		URL_MAP.put(Constants.PREFIX_VKOL, "/AccountSaml/SignIn/?idp=https%3A%2F%2Fshibo.vkol.cz%2Fidp%2Fshibboleth&returnUrl=");
	}

	public BookportMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) {
				results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getLibrary().getName().toLowerCase(),
						Constants.DOCUMENT_AVAILABILITY_MEMBER, parseUrl(df.getSubfield('u').getData()), COMMENT));
			}
		}
		return results;
	}

	private String parseUrl(String f856u) {
		Matcher matcher = URL_PATTERN.matcher(f856u);
		String prefix = harvestedRecord.getHarvestedFrom().getLibrary().getName().toLowerCase();
		if (matcher.matches() && URL_MAP.containsKey(prefix)) {
			return matcher.group(1) + URL_MAP.get(prefix) + matcher.group(2);
		}
		return f856u;
	}

	@Override
	public List<String> getCustomInstitutionFacet() {
		return SolrUtils.createHierarchicFacetValues(SolrUtils.INSTITUTION_OTHERS, "ebook", Constants.PREFIX_BOOKPORT.toUpperCase());
	}

	@Override
	public boolean matchFilterBookport() {
		return true;
	}

	@Override
	public List<String> filterBookportUrls(List<String> urls) {
		return urls;
	}

}
