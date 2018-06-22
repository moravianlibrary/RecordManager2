package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class LibraryMetadataMarcRecord extends MetadataMarcRecord {

	@Autowired
	private HarvestedRecordDAO hrDao;

	private static final Pattern LIBRARY_CLOSED = Pattern
			.compile("KNIHOVNA ZRUŠENA!");
	private static final Pattern INSTITUTION_CLOSED = Pattern
			.compile("ZRUŠENÁ INSTITUCE!");

	private static final Map<String, String> siglaCache = new ConcurrentHashMap<>(16, 0.9f, 1);

	public LibraryMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		if (underlayingMarc.getDataFields("DEL") != null
				&& !underlayingMarc.getDataFields("DEL").isEmpty()) {
			return false;
		}
		for (DataField df : underlayingMarc.getDataFields("STT")) {
			Subfield sf;
			if ((sf = df.getSubfield('a')) != null
					&& (LIBRARY_CLOSED.matcher(sf.getData()).matches() || INSTITUTION_CLOSED
							.matcher(sf.getData()).matches())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String getLibrarySigla() {
		return underlayingMarc.getField("SGL", 'a');
	}

	@Override
	public String getRegionalLibrary() {
		for (DataField df : underlayingMarc.getDataFields("FCE")) {
			for (Subfield sf : df.getSubfields('a')) {
				String sigla = underlayingMarc.getField("PVK", 's');
				String name = underlayingMarc.getField("PVK", 'n');
				if (!sf.getData().equalsIgnoreCase("obsluhovaná regionální funkcí")
						|| sigla == null || name == null) continue;
				String result;
				if (siglaCache.containsKey(sigla)) {
					result = siglaCache.get(sigla);
				} else {
					String id = hrDao.getIdBySigla(sigla);
					if (id == null) return null;
					result = id + '|' + name;
					siglaCache.put(sigla, result);
				}
				return result;
			}
		}
		return null;
	}
}
