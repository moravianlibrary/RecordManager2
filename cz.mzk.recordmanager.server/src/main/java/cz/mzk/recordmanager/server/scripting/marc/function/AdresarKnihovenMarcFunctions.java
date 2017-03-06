package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class AdresarKnihovenMarcFunctions implements MarcRecordFunctions {
	
	private final static Pattern FIELD_PATTERN = Pattern.compile("([a-zA-Z0-9]{3})([a-zA-Z0-9]*)");
	
	public String getFirstFieldForAdresar(MarcFunctionContext ctx, String tag) {
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: " + tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		return ctx.record().getField(fieldTag, subFields.toCharArray());
	}

	public String getFirstFieldSeparatedForAdresar(MarcFunctionContext ctx, String tag, String separator) {
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: " + tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		return ctx.record().getField(fieldTag, separator, subFields.toCharArray());
	}

	public List<String> getFieldsForAdresar(MarcFunctionContext ctx, String tags, SubfieldExtractionMethod method, String separator) {
		List<String> result = new ArrayList<>();
		for (String tag : tags.split(":")) {
			Matcher matcher = FIELD_PATTERN.matcher(tag);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Tag can't be parsed: " + tag);
			}
			String fieldTag = matcher.group(1);
			String subFields = matcher.group(2);
			result.addAll(ctx.record().getFields(fieldTag, null, method, separator, subFields.toCharArray()));
		}
		return result;
	}
	
	public List<String> adresarGetResponsibility(MarcFunctionContext ctx) {
		List<String> results = new ArrayList<>();
		char[] sfCodes = new char[]{'t', 'k', 'p', 'r', 'f', 'e'};
		String[] separators = new String[]{" ", " ", " (", ") ; ", " ; ", ""};
		for (DataField df : ctx.record().getDataFields("JMN")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < separators.length; i++) {
				if (df.getSubfield(sfCodes[i]) != null) {
					sb.append(df.getSubfield(sfCodes[i]).getData());
				}
				sb.append(separators[i]);
			}
			results.add(sb.toString().trim());
		}
		return results;
	}

	public List<String> adresarGetAddress(MarcFunctionContext ctx) {
		List<String> results = new ArrayList<>();
		char[] sfCodes = new char[]{'u', 'c', 'm', 'p', 'q'};
		String[] separators = new String[]{", ", " ", " (", ") [", "]"};
		for (DataField df : ctx.record().getDataFields("ADR")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < separators.length; i++) {
				if (df.getSubfield(sfCodes[i]) != null) {
					sb.append(df.getSubfield(sfCodes[i]).getData());
				}
				sb.append(separators[i]);
			}
			results.add(sb.toString().trim());
		}
		return results;
	}
	
	public List<String> adresarGetEmailOrMvs(MarcFunctionContext ctx, String tag) {
		List<String> results = new ArrayList<>();
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: " + tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		for (DataField df : ctx.record().getDataFields(fieldTag)) {
			StringBuilder sb = new StringBuilder();
			if (df.getSubfield(subFields.charAt(0)) != null) {
				sb.append(df.getSubfield(subFields.charAt(0)).getData());
			}
			if (df.getSubfield(subFields.charAt(1)) != null) {
				sb.append(" (");
				sb.append(df.getSubfield(subFields.charAt(1)).getData());
				sb.append(")");
			}
			if (!sb.toString().isEmpty()) results.add(sb.toString().trim());
		}
		return results;
	}
	
	public List<String> adresarGetRegionDistrictFacet(MarcFunctionContext ctx) {
		String region = getFirstFieldForAdresar(ctx, "KRJa");
		String district = getFirstFieldForAdresar(ctx, "KRJb");
		if (region != null && district != null) {
			return SolrUtils.createHierarchicFacetValues(getFirstFieldForAdresar(ctx, "KRJa"), getFirstFieldForAdresar(ctx, "KRJb"));
		}
		return null;
	}
	
	public List<String> adresarGetNameAlt(MarcFunctionContext ctx, String separator) {
		List<String> results = new ArrayList<>();
		for (DataField df : ctx.record().getDataFields("VAR")) {
			if (df.getIndicator1() == '2') {
				StringBuilder sb = new StringBuilder();
				for (char code : new char[]{'a', 'b', 'c'}) {
					if (df.getSubfield(code) != null) {
						sb.append(df.getSubfield(code).getData());
						sb.append(separator);
					}
				}
				String temp = sb.toString();
				if (temp.endsWith(separator)) temp = temp.substring(0, temp.length() - separator.length());
				results.add(temp);
			}
		}
		return results;
	}

}
