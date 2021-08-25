package cz.mzk.recordmanager.server.metadata.mappings996;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.marc4j.marc.DataField;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultMappings996 implements Mappings996 {

	private static final List<String> AVAILABILITY_STOP_WORDS = Arrays.asList("N", "NZ", "F");
	private static final List<String> AGENCY_ID_STOP_WORDS = Collections.singletonList("7");

	@Override
	public boolean ignore(DataField df) {
		return df.getSubfield('q') != null && df.getSubfield('q').getData().equals("0");
	}

	@Override
	public String getItemId(DataField df) {
		return df.getSubfield('b') != null ? df.getSubfield('b').getData() : "";
	}

	@Override
	public String getCallnumber(DataField df) {
		return df.getSubfield('c') != null ? df.getSubfield('c').getData() : "";
	}

	@Override
	public String getDepartment(DataField df) {
		return df.getSubfield('l') != null ? df.getSubfield('l').getData() : "";
	}

	@Override
	public String getLocation(DataField df) {
		return df.getSubfield('h') != null ? df.getSubfield('h').getData() : "";
	}

	@Override
	public String getDescription(DataField df) {
		return df.getSubfield('d') != null ? df.getSubfield('d').getData() : "";
	}

	@Override
	public String getNotes(DataField df) {
		return df.getSubfield('p') != null ? df.getSubfield('p').getData() : "";
	}

	@Override
	public String getYear(DataField df) {
		return df.getSubfield('y') != null ? df.getSubfield('y').getData() : "";
	}

	@Override
	public String getVolume(DataField df) {
		return df.getSubfield('v') != null ? df.getSubfield('v').getData() : "";
	}

	@Override
	public String getIssue(DataField df) {
		return df.getSubfield('i') != null ? df.getSubfield('i').getData() : "";
	}

	@Override
	public String getAvailability(DataField df) {
		return df.getSubfield('s') != null && !AVAILABILITY_STOP_WORDS.contains(df.getSubfield('s').getData())
				? df.getSubfield('s').getData() : "";
	}

	@Override
	public String getCollectionDesc(DataField df) {
		return df.getSubfield('r') != null ? df.getSubfield('r').getData() : "";
	}

	@Override
	public String getAgencyId(DataField df) {
		return df.getSubfield('9') != null && !AGENCY_ID_STOP_WORDS.contains(df.getSubfield('9').getData())
				? df.getSubfield('9').getData() : "";
	}

	@Override
	public String getSequenceNo(DataField df) {
		return "";
	}

	@Override
	public String getSubfieldW(DataField df) {
		return df.getSubfield('w') != null ? df.getSubfield('w').getData() : "";
	}

	@Override
	public String getCaslinUrl(DataField df) {
		return "";
	}

	@Override
	public String getMappingAsCsv(DataField df) {
		StringWriter writer = new StringWriter();
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL));
			printer.printRecord(
					getItemId(df),
					getCallnumber(df),
					getDepartment(df),
					getLocation(df),
					getDescription(df),
					getNotes(df),
					getYear(df),
					getVolume(df),
					getIssue(df),
					getAvailability(df),
					getCollectionDesc(df),
					getAgencyId(df),
					getSequenceNo(df),
					getSubfieldW(df),
					getCaslinUrl(df)
			);
			printer.flush();
			return writer.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getMappingAsCsv(List<DataField> dfs) {
		List<String> results = new ArrayList<>();
		for (DataField df : dfs) {
			if (this.ignore(df)) continue;
			results.add(this.getMappingAsCsv(df));
		}
		return results;
	}

}
