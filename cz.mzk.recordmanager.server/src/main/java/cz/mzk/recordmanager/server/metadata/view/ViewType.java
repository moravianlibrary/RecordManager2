package cz.mzk.recordmanager.server.metadata.view;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import cz.mzk.recordmanager.server.util.Constants;

import java.io.IOException;
import java.util.*;

public enum ViewType {
	IREL("irel") {
		@Override
		protected boolean match(final MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			if (mr.isBlindBraille() || mr.isMusicalScores() || mr.isVisualDocument()) return false;
			if (importConfId.equals(Constants.IMPORT_CONF_ID_UNMZ) || importConfId.equals(Constants.IMPORT_CONF_ID_UPV))
				return false;
			return contains(resolver, String.format(INST_FILE, getValue()), importConfId.toString())
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);

		}
	},
	TECH("tech") {
		@Override
		protected boolean match(final MetadataRecord mr, ListResolver resolver, Long importConfId,
								Set<String> siglas, List<String> conspectus) throws IOException {
			if (mr.isBlindBraille() || mr.isMusicalScores()) return false;
			return contains(resolver, String.format(INST_FILE, getValue()), importConfId.toString())
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
			return contains(resolver, String.format(INST_FILE, getValue()), importConfId.toString())
					|| containsAny(resolver, String.format(CONSPECTUS_FILE, getValue()), conspectus);
		}
	};

	private String value;

	protected static final String INST_FILE = "view/%s.txt";
	protected static final String CASLIN_FILE = "view/%s_caslin.txt";
	protected static final String CONSPECTUS_FILE = "view/%s_conspectus.txt";

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
