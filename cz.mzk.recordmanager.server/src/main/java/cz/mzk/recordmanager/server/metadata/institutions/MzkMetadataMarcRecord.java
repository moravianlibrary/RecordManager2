package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MatchAllDataFieldMatcher;
import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MzkMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern CLUSTER_ID_PATTERN = Pattern.compile("00.*");

	public MzkMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getClusterId() {
		String f001 = underlayingMarc.getControlField("001");
		if (!CLUSTER_ID_PATTERN.matcher(f001).matches()) {
			return f001;
		}
		return null;
	}

	@Override
	public String getCallnumber() {
		List<String> results = new ArrayList<>();
		Set<String> callNumbers = new HashSet<>();
		callNumbers.addAll(underlayingMarc.getFields("910", MatchAllDataFieldMatcher.INSTANCE,
				SubfieldExtractionMethod.SEPARATED, "", 'b'));
		callNumbers.addAll(underlayingMarc.getFields("996", MatchAllDataFieldMatcher.INSTANCE,
				SubfieldExtractionMethod.SEPARATED, "", 'c', 'h'));
		results.addAll(callNumbers.stream().filter(field -> !field.startsWith("TK") && !field.startsWith("PK"))
				.collect(Collectors.toList()));
		if (results.isEmpty() && !callNumbers.isEmpty()) {
			results.addAll(callNumbers);
		}
		return StringUtils.join(results, ';');
	}
}
