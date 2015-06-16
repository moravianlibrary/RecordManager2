package cz.mzk.recordmanager.server.scripting.dc.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;

@Component
public class IdentifiersDublinCoreRecordFunctions implements
		DublinCoreRecordFunctions {

	private static final Pattern ISBN_PATTERN = Pattern.compile("isbn|ISBN:([\\d]+)");

	public List<String> getISBNs(DublinCoreRecord record) {
		List<String> isbns = new ArrayList<String>();
		for (String ident : record.getIdentifiers()) {
			Matcher matcher = ISBN_PATTERN.matcher(ident);
			if (matcher.matches()) {
				isbns.add(matcher.group(1));
			}
		}
		return isbns;
	}

}
