package cz.mzk.recordmanager.server.metadata.view;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import cz.mzk.recordmanager.server.util.Constants;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public enum ViewType {
	IREL("irel") {
		@Override
		protected boolean match(final MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			if (mr.isBlindBraille() || mr.isMusicalScores() || mr.isVisualDocument()) return false;
			if (importConfId.equals(Constants.IMPORT_CONF_ID_UNMZ) || importConfId.equals(Constants.IMPORT_CONF_ID_UPV))
				return false;
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString())
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);

		}
	},
	TECH("tech") {
		@Override
		protected boolean match(final MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			if (mr.isBlindBraille() || mr.isMusicalScores()) return false;
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString())
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);
		}
	},
	MUS("mus") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			return mr.isMusicalScores()
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);
		}
	},
	KIV("kiv") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			if (importConfId.equals(Constants.IMPORT_CONF_ID_ANL)) return false;
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString())
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);
		}
	},
	BRNO("brno") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString());
		}
	},
	CPK("cpk") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			return !contains(resolver, String.format(INST_EXCLUDE_FILE, getValue()), importConfId.toString());
		}
	},
	MZK("mzk") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString());
		}
	},
	GEOBIBLINE("geobibline") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
				Set<String> siglas, List<String> conspectus) throws IOException {
			if (Collections.disjoint(Arrays.asList("xr", "cs"), mr.getCountries())
					&& !mr.getLanguages().contains("cze")) return false;
			Set<String> formats = mr.getDetectedFormatList().stream().map(f -> f.toString()).collect(Collectors.toSet());
			if (containsAny(resolver, String.format(FORMAT_FILE, getValue()), formats)) {
				return true;
			}
			if (containsAny(resolver, String.format(TOPIC_FILE, getValue()), mr.getTopic())
					&& containsAny(resolver, String.format("view/%s_topic_format.txt", getValue()), formats)) {
				return true;
			}
			if (containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus)) {
				return true;
			}
			Set<String> mdt = mr.getMdt();
			if (containsAny(resolver, String.format(MDT_FILE, getValue()), mdt)) {
				return true;
			}
			if (mdt.stream().anyMatch(m -> m.startsWith("(084.3"))) return true;
			return false;
		}
	},
	NKP("nkp") {
		@Override
		protected boolean match(MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			return contains(resolver, String.format(INST_INCLUDE_FILE, getValue()), importConfId.toString());
		}
	};

	private String value;

	protected static final String INST_INCLUDE_FILE = "view/%s.txt";
	protected static final String INST_EXCLUDE_FILE = "view/%s_exclude.txt";
	protected static final String CASLIN_FILE = "view/%s_caslin.txt";
	protected static final String CONSPECTUS_FILE = "view/%s_conspectus.txt";
	protected static final String FORMAT_FILE = "view/%s_format.txt";
	protected static final String TOPIC_FILE = "view/%s_topic.txt";
	protected static final String MDT_FILE = "view/%s_mdt.txt";

	ViewType(String value) {
		this.value = value;
	}

	/**
	 * @param mr {@link MetadataRecord} of actual record
	 * @return {@link List} of possible {@link ViewType} values for input {@link MetadataRecord}
	 */
	public static List<String> getPossibleValues(final MetadataRecord mr, ListResolver resolver, Long config) {
		List<String> results = new ArrayList<>();
		Set<String> siglas = mr.getCaslinSiglas();
		List<String> conspectus = mr.getConspectusForView();
		for (ViewType viewType : ViewType.values()) {
			try {
				if (viewType.match(mr, resolver, config, siglas, conspectus)) results.add(viewType.getValue());
			} catch (IOException ignore) {
			}
		}
		return results;
	}

	protected boolean contains(ListResolver resolver, String listFile, String value) throws IOException {
		return resolver.resolve(listFile).contains(value);
	}

	protected boolean containsAny(ListResolver resolver, String listFile, Collection<String> values) throws IOException {
		return !Collections.disjoint(resolver.resolve(listFile), values);
	}

	public String getValue() {
		return value;
	}

	protected abstract boolean match(final MetadataRecord mr, ListResolver resolver, Long importConfId,
									 Set<String> siglas, List<String> conspectus) throws IOException;

}
