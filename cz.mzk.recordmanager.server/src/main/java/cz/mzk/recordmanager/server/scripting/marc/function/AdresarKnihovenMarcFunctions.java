package cz.mzk.recordmanager.server.scripting.marc.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;
import cz.mzk.recordmanager.server.model.Sigla;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.SiglaDAO;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class AdresarKnihovenMarcFunctions implements MarcRecordFunctions {
	
	@Autowired
	private SiglaDAO siglaDao;
	
	@Autowired
	private ImportConfigurationDAO configurationDAO;
	
	private static final MappingResolver propertyResolver = new ResourceMappingResolver(new ClasspathResourceProvider());
	
	private final static Pattern FIELD_PATTERN = Pattern.compile("([a-zA-Z0-9]{3})([a-zA-Z0-9]*)");
	private final static Pattern GPS_PATTERN = Pattern.compile("(\\d+)°(\\d+)'([\\d\\.]+)\"([NSEW])");
	private final static String MAP_ADRESAR_HOURS = "adresar_hours.map";
	
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
		Subfield sf;
		for (DataField df : ctx.record().getDataFields("JMN")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < sfCodes.length; i++) {
				if ((sf = df.getSubfield(sfCodes[i])) == null) continue;
				switch (sf.getCode()) {
				case 'k':
				case 'p':
					sb.append(" ");
					sb.append(sf.getData());
					break;
				case 'r':
					sb.append(" (");
					sb.append(sf.getData());
					sb.append(")");
					break;
				case 'f':
				case 'e':
					sb.append(" ; ");
					sb.append(sf.getData());
					break;
				default:
					sb.append(sf.getData());
					break;
				}
			}
			results.add(sb.toString().trim());
		}
		return results;
	}

	public List<String> adresarGetAddress(MarcFunctionContext ctx) {
		List<String> results = new ArrayList<>();
		char[] sfCodes = new char[]{'u', 'c', 'm', 'p', 'g'};
		Subfield sf;
		for (DataField df : ctx.record().getDataFields("ADR")) {
			StringBuilder sb = new StringBuilder();
			boolean isData = false;
			for (int i = 0; i < sfCodes.length; i++) {
				if ((sf = df.getSubfield(sfCodes[i])) == null) continue;
				switch (sf.getCode()) {
				case 'c':
					if (isData) sb.append(", ");
					sb.append(sf.getData());
					break;
				case 'm':
					if (isData) sb.append(" ");
					sb.append(sf.getData());
					break;
				case 'p':
					if (isData) sb.append(" ");
					sb.append("(");
					sb.append(sf.getData());
					sb.append(")");
					break;
				case 'g':
					if (isData) sb.append(" ");
					sb.append("[");
					sb.append(sf.getData());
					sb.append("]");
					break;
				default:
					sb.append(sf.getData());
					break;
				}
				isData = true;
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
	
	public String adresarGetHours(MarcFunctionContext ctx) {
		String separator = " | ";
		for (DataField df : ctx.record().getDataFields("OTD")) {
			StringBuilder sb = new StringBuilder();
			for (Subfield sf : df.getSubfields()) {
				try {
					List<String> get = propertyResolver.resolve(MAP_ADRESAR_HOURS).get(String.valueOf(sf.getCode()));
					if (get != null) {
						sb.append(get.get(0) + " " + sf.getData());
						sb.append(separator);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String temp = sb.toString();
			if (temp != null && !temp.isEmpty() && temp.endsWith(separator)) {
				temp = temp.substring(0, temp.length() - separator.length());
			}
			return temp;
		}
		
		return null;
	}
	
	public String adresarGetCpkCode(MarcFunctionContext ctx) {
		String siglaName;
		if ((siglaName = getFirstFieldForAdresar(ctx, "SGLa")) != null) {
			for (Sigla sigla : siglaDao.findSiglaByName(siglaName)) {
				return configurationDAO.get(sigla.getUniqueId().getImportConfId()).getIdPrefix();
			}
		}
		return null;
	}

	public String adresarGetGps(MarcFunctionContext ctx) {
		Matcher matcher;
		for (String data : ctx.record().getFields("ADR", 'g')) {
			matcher = GPS_PATTERN.matcher(data);
			String latitude = null, longitude = null;
			while (matcher.find()) {
				double i = Double.valueOf(matcher.group(1))
						+ (Double.valueOf(matcher.group(2)) / 60)
						+ (Double.valueOf(matcher.group(3)) / 3600)
						* (matcher.group(4).toLowerCase().equals("s|w") ? -1 : 1);
				if (latitude == null) latitude = String.valueOf(i);
				else longitude = String.valueOf(i);
			}
			if (latitude != null && longitude != null) return latitude + " " + longitude;
		}
		return null;
	}

}
