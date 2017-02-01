package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.marc4j.marc.DataField;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;

@Component
public class PublishDateMarcFunctions implements MarcRecordFunctions {

	private static final int MIN_YEAR = 1199;
	private static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR);

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

	// [1991] or asi 1991
	private static final Pattern FOUR_DIGIT_YEAR_PATTERN = Pattern
			.compile("\\d{4}");
	
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
			} else if ((matcher = FOUR_DIGIT_YEAR_PATTERN.matcher(range)).find()){
				int year = Integer.parseInt(matcher.group(0));
				if(year == 9999) year = MAX_YEAR;
				result.add(year);
			} else {
//				logger.warn("Range '{}' not matched", range);
			}
		}
		return result;
	}

	public Set<Integer> getPublishDate(MarcFunctionContext ctx) {
		MarcRecord record = ctx.record();
		Set<Integer> years = new TreeSet<Integer>();

		years.addAll(getPublishDateFromFields(ctx));

        String field008 = record.getControlField("008");
        years.addAll(parsePublishDateFrom008(field008));

        return years;
	}

	public Set<Integer> getPublishDateFromFields(MarcFunctionContext ctx){
		MarcRecord record = ctx.record();
		Set<Integer> years = new TreeSet<Integer>();

		for(DataField datafield: record.getDataFields("264")){
			if(datafield.getIndicator2() == '1'){
				if(datafield.getSubfield('c') != null){
					years.addAll(getPublishDateFromItem(datafield.getSubfield('c').getData()));
				}
			}
		}
		years.addAll(getPublishDateFromItems(ctx, "260", 'c'));
		years.addAll(getPublishDateFromItems(ctx, "773", '9'));
		years.addAll(getPublishDateFromItems(ctx, "996", 'y'));

		return years;
	}

	public Set<Integer> parsePublishDateFrom008(String field008) {
		Set<Integer> result = Sets.newHashSet();
		if (field008 == null || field008.length() < 12) {
            return result;
        }
		char type = field008.charAt(6);
		if(type == 'b' || type == 'n'){
			return result;
		}
		String fromString = field008.substring(7, 11);
		int from = 0;
		if((SINGLE_YEAR_PATTERN.matcher(fromString)).matches()){
			from = Integer.parseInt(fromString);
		}
		else return result;
		if(type == 'e' || type == 'i' || type == 'k' || type == 's' || type == 'u' || type == 'm'){
			result.add(from);
		}		
		if(field008.length() < 16){
			return result;
		}
		
		String toString = field008.substring(11, 15).trim();
		int to = 0;
		if((SINGLE_YEAR_PATTERN.matcher(toString)).matches()){
			to = Integer.parseInt(toString);
		}
		else return result;
		
		if(type == 'd' || type == 'q' || type == 'c'){
			if (to > MAX_YEAR) {
	            to = MAX_YEAR;
	        }
			for (int year = from; year <= to; year++) {
				result.add(year);
			}
		}
		if(type == 'p' || type == 'r' || type == 't'){		
			result.add(from);
			result.add(to);
		}
		
		return result;
	}

	private Set<Integer> getPublishDateFromItem(String data) {
		List<String> ranges = new ArrayList<String>();
		ranges.add(data);
		return parseRanges(ranges);
	}
	
	private Set<Integer> getPublishDateFromItems(MarcFunctionContext ctx, String tag, char subfield) {
		MarcRecord record = ctx.record();
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

	public String getPublishDateForSorting(MarcFunctionContext ctx){
		if(ctx.metadataRecord().getDetectedFormatList().contains(HarvestedRecordFormatEnum.ARTICLES)){
			return getPublishDateForSortingForArticles(ctx);
		}
		else{
			return getPublishDateForSortingForOthers(ctx);
		}
	}
	
	private String getPublishDateForSortingForArticles(MarcFunctionContext ctx){
		for(String year: ctx.record().getFields("773", "", '9')){
			if(year.length() > 4) year = year.substring(0, 4);
			if (SINGLE_YEAR_PATTERN.matcher(year).matches()) {
				int yearInt = Integer.parseInt(year);
				if (MIN_YEAR < yearInt && yearInt <= MAX_YEAR+1) {
					return year;
				}
				
			}
		}
		
		MarcRecord mr = ctx.record();
		String field008 = mr.getControlField("008");
		if(field008 != null && field008.length() >= 12){
			String yearS = field008.substring(7, 11);
			if(SINGLE_YEAR_PATTERN.matcher(yearS).matches()) {
				 int yearI = Integer.parseInt(yearS);
				 if((1500 < yearI) && (yearI < (MAX_YEAR+1))){
					 return yearS;
				 }
			}
		}
		return null;
	}
	
	private String getPublishDateForSortingForOthers(MarcFunctionContext ctx) {
		Set<Integer> years = getPublishDateFromFields(ctx);

		years = getPublishDateFromFields(ctx);

		MarcRecord mr = ctx.record();
		String field008 = mr.getControlField("008");

		if (field008 != null && field008.length() >= 12) {
			char type = field008.charAt(6);
			if(type == 'd' || type == 'q' || type == 'c'){
				years.addAll(parsePublishDateFrom008(field008));
			}
			else{
				if(field008.length() > 15){
					String toString = field008.substring(11, 15).trim();
					int to = 0;
					if((SINGLE_YEAR_PATTERN.matcher(toString)).matches()){
						to = Integer.parseInt(toString);
					}
					if(to <= MAX_YEAR+1) years.addAll(parsePublishDateFrom008(field008));
				}
				else if(field008.length() > 11){
					String fromString = field008.substring(7, 11);
					int from = 0;
					if((SINGLE_YEAR_PATTERN.matcher(fromString)).matches()){
						from = Integer.parseInt(fromString);
					}
					if(from <= MAX_YEAR+1) years.addAll(parsePublishDateFrom008(field008));
				}
			}
        }
		if (!years.isEmpty()) {
			years.removeIf(year -> year <= MIN_YEAR || MAX_YEAR+1 < year);
			if (!years.isEmpty()) {
				return years.iterator().next().toString();
			}
		}
		return null;
	}
	
	public String getPublishDateDisplay(MarcFunctionContext ctx) {
		for (String tag: new String[]{"260","264"}) {
			for (DataField df: ctx.record().getDataFields(tag)) {
				if (tag.equals("264") && df.getIndicator2() != '1') {
					continue;
				}
				if(df.getSubfield('c') != null) {
					String year = df.getSubfield('c').getData().trim();
					if(year.length() > 0 && year.endsWith("]") && !year.startsWith("[")){ 
						return "[" + year;
					}
					return year;
				}
			}
		}
		return null;
	}

}
