package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class PublishDateMarcFunctions implements MarcRecordFunctions {

	private static Logger logger = LoggerFactory
			.getLogger(PublishDateMarcFunctions.class);

	private static final int INVALID_YEAR = 0;

	private static final int MAX_YEAR = 2020;

	// 2004
	private static final Pattern SINGLE_YEAR_PATTERN = Pattern
			.compile("^[0-9]{4}$");

	// 2001-2005, 2001/2002
	private static final Pattern FULL_RANGE_YEAR_PATTERN = Pattern
			.compile("^([0-9]{4})[\\-|\\/]([0-9]{4})$");

	// 1950-54
	private static final Pattern SHORTEN_RANGE_YEAR_PATTERN = Pattern
			.compile("^([0-9]{4})[\\-|\\/]([0-9]{2})$");

	// 1989,1990,1991
	private static final Pattern LIST_YEAR_PATTERN = Pattern
			.compile("^([0-9]{4})(,[0-9]{4})+[,]{0,1}$");

	public Set<Integer> parseRanges(Collection<String> ranges) {
		Set<Integer> result = new TreeSet<>();
		for (String range : ranges) {
			range = range.trim();
			if (range.isEmpty()) {
				continue;
			}
			Matcher matcher;
			if (SINGLE_YEAR_PATTERN.matcher(range).matches()) {
				result.add(Integer.parseInt(range));
			} else if ((matcher = FULL_RANGE_YEAR_PATTERN.matcher(range))
					.matches()) {
				result.addAll(range(Integer.parseInt(matcher.group(1)),
						Integer.parseInt(matcher.group(2))));
			} else if ((matcher = SHORTEN_RANGE_YEAR_PATTERN.matcher(range))
					.matches()) {
				int start = Integer.parseInt(matcher.group(1));
				int end = Integer.parseInt(matcher.group(1).substring(0, 2)
						+ matcher.group(2));
				result.addAll(range(start, end));
			} else if ((matcher = LIST_YEAR_PATTERN.matcher(range)).matches()) { // 1989,1990,1991
				String[] years = matcher.group(0).split(",");
				for (String year : years) {
					result.add(Integer.parseInt(year));
				}
			} else {
				logger.warn("Range '{}' not matched", range);
			}
		}
		return result;
	}

	public Set<Integer> getPublishDate(MarcRecord record) {
        Set<Integer> years = getPublishDateFromItems(record, "996", 'y');
        if (!years.isEmpty()) {
            return years;
        }
        String field008 = record.getControlField("008");
        return parsePublishDateFrom008(field008);
	}

	public Set<Integer> parsePublishDateFrom008(String field008) {
		Set<Integer> result = Sets.newHashSet();
		if (field008 == null || field008.length() < 16) {
            return result;
        }
		char type = field008.charAt(6);
		int from = toYear(field008.substring(7, 11), '0');
		int to = toYear(field008.substring(11, 15).trim(), '9');
		if (type == 'e' || to == INVALID_YEAR) {
			to = from;
		}
		if (to == INVALID_YEAR) {
			return result;
		}
		if (to > MAX_YEAR) {
            to = MAX_YEAR;
        }
		for (int year = from; year <= to; year++) {
			result.add(year);
		}
		return result;
	}

	private int toYear(String year, char replaceWith) {
		try {
			String fixed = year.replace('u', replaceWith).replace('?', replaceWith).replace(' ', replaceWith);
			return Integer.parseInt(fixed);
		} catch (NumberFormatException nfe) {
			return INVALID_YEAR;
		}
	}

	private Set<Integer> getPublishDateFromItems(MarcRecord record, String tag, char subfield) {
		List<String> ranges = record.getFields(tag, "", subfield) ;
		return parseRanges(ranges);
	}

	private List<Integer> range(int from, int to) {
		IntStream stream = IntStream.rangeClosed(from, to);
		List<Integer> result = new ArrayList<Integer>();
		for (int year : stream.toArray()) {
			result.add(year);
		}
		return result;
	}

	public String getPublishDateForSorting(MarcRecord record) {
		return null; // FIXME
	}

}
