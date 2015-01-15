package cz.mzk.recordmanager.server.dedup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class MarcXmlDedupKeyParser implements DedupKeysParser {

	private final static String FORMAT = "marc21-xml";

	private final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

	@Autowired
	private MarcXmlParser parser;

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat()));
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		try {
			MarcRecord rec = parser.parseRecord(is);
			record.setIsbn(getIsbn(rec));
		} catch (InvalidMarcException ime) {
			throw new DedupKeyParserException("Record can't be parsed", ime);
		}
		return record;
	}

	protected String getIsbn(MarcRecord rec) {
		String isbn = rec.getField("020", 'a');
		if (isbn == null) {
			return null;
		}
		return isbnValidator.validate(isbn);
	}

}
