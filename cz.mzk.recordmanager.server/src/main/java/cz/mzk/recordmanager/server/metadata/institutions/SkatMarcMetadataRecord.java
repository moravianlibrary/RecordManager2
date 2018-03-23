package cz.mzk.recordmanager.server.metadata.institutions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.metadata.ViewType;
import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class SkatMarcMetadataRecord extends MetadataMarcRecord {

	public SkatMarcMetadataRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	private static final List<String> IREL_SIGLAS = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/mapping/view_irel_siglas.map"), StandardCharsets.UTF_8))
			.lines().collect(Collectors.toCollection(ArrayList::new));

	@Override
	public String getUUId() {
		String baseStr = underlayingMarc.getField("911", 'u');
		if (baseStr == null) {
			return null;
		}

		Matcher matcher = UUID_PATTERN.matcher(baseStr);
		if (matcher.find()) {
			String uuidStr = matcher.group(0);
			if (uuidStr != null && uuidStr.length() > 5) {
				return uuidStr.substring(5);
			}
		}
		return null;
	}
	
	/**
	 * filtered when there isn't any subfield q = 0
	 */
	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<ViewType> getViewType() {
		for (String data : underlayingMarc.getFields("996", 'e')) {
			if (IREL_SIGLAS.contains(data.trim()) && isIrelView())
				return Collections.singletonList(ViewType.IREL);
		}
		return Collections.emptyList();
	}
}
