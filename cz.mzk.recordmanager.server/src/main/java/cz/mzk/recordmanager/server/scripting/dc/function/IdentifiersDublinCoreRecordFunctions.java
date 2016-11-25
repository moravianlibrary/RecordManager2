package cz.mzk.recordmanager.server.scripting.dc.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreFunctionContext;

@Component
public class IdentifiersDublinCoreRecordFunctions implements
		DublinCoreRecordFunctions {

	private static final Pattern ISBN_PATTERN = Pattern.compile("isbn|ISBN:([\\d]+)");
	private static final Pattern ISSN_PATTERN = Pattern.compile("(\\d{4}-\\d{3}[\\dxX])(.*)");

	public List<String> getISBNs(DublinCoreFunctionContext dcContext) {
		List<String> isbns = new ArrayList<String>();
		DublinCoreRecord record = dcContext.record();
		for (String ident : record.getIdentifiers()) {
			Matcher matcher = ISBN_PATTERN.matcher(ident);
			if (matcher.matches()) {
				isbns.add(matcher.group(1));
			}
		}
		return isbns;
	}

	public List<String> getISSNs(DublinCoreFunctionContext dcContext) {
		List<String> issns = new ArrayList<String>();
		DublinCoreRecord record = dcContext.record();
		for (String ident : record.getIdentifiers()) {
			Matcher matcher = ISSN_PATTERN.matcher(ident);
			if (matcher.matches()) {
				issns.add(matcher.group(1));
			}
		}
		return issns;
	}
}
