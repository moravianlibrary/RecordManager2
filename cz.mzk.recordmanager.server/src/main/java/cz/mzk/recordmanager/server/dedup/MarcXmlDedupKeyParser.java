package cz.mzk.recordmanager.server.dedup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.marc4j.MarcXmlReader;
import org.marc4j.marc.InvalidMARCException;
import org.marc4j.marc.Record;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.marc.MarcHelper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class MarcXmlDedupKeyParser implements DedupKeysParser {

	private final static String FORMAT = "marc21-xml";

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat()));
		try {
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcXmlReader reader = new MarcXmlReader(is);
			Record rec = reader.next();
			String isbn = getIsbn(rec);
			record.setIsbn(isbn);
		} catch (InvalidMARCException ime) {
			throw new DedupKeyParserException("Dedup keys can't be parsed", ime);
		}
		return record;
	}

	protected String getIsbn(Record rec) {
		return MarcHelper.getField(rec, "020", 'a');
	}

}
