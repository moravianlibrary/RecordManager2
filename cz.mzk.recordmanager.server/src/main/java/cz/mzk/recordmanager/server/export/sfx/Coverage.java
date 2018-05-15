package cz.mzk.recordmanager.server.export.sfx;

import cz.mzk.recordmanager.server.util.constants.SfxConstants;
import org.marc4j.marc.DataField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coverage implements Comparable {

	private String from;
	private String to;
	private String embargo;

	private char embargoType;
	private String embargoDays;

	private static final Pattern EMBARGO_PATTERN = Pattern.compile("([an])([0-9]*)");

	private Coverage(String from, String to, String embargo) {
		this.from = from == null ? "" : from;
		this.to = to == null ? "" : to;
		this.embargo = embargo;
		Matcher matcher;
		if ((matcher = EMBARGO_PATTERN.matcher(embargo)).matches()) {
			this.embargoType = matcher.group(1).charAt(0);
			this.embargoDays = matcher.group(2);
		} else embargoType = ' ';
	}

	public static List<Coverage> create(List<DataField> coverages) {
		List<Coverage> results = new ArrayList<>();
		for (DataField df : coverages) {
			results.add(new Coverage(
					df.getSubfield('a') != null ? df.getSubfield('a').getData() : "",
					df.getSubfield('b') != null ? df.getSubfield('b').getData() : "",
					df.getSubfield('c') != null ? df.getSubfield('c').getData() : ""
			));
		}
		return results;
	}

	public void addToXml(Document doc) {
		Node root = doc.getFirstChild();
		Element coverage = doc.createElement(SfxConstants.ELEMENT_COVERAGE);
		if (!from.isEmpty() || !to.isEmpty()) {
			coverage.appendChild(createYear(doc, SfxConstants.ELEMENT_FROM, from));
			coverage.appendChild(createYear(doc, SfxConstants.ELEMENT_TO, to));
		}
		if (embargoType != ' ') coverage.appendChild(createEmbargo(doc));
		root.appendChild(coverage);
	}

	private Element createYear(Document doc, String name, String year) {
		Element element = doc.createElement(name);
		if (!year.isEmpty()) {
			Element elYear = doc.createElement(SfxConstants.ELEMENT_YEAR);
			elYear.setTextContent(year);
			element.appendChild(elYear);
		}
		return element;
	}

	private Element createEmbargo(Document doc) {
		Element embargo = doc.createElement(SfxConstants.ELEMENT_EMBARGO);
		Element days_available = doc.createElement(embargoType == 'a' ? SfxConstants.ELEMENT_DAYS_AVAILABLE : SfxConstants.ELEMENT_DAYS_NOT_AVAILABLE);
		days_available.setTextContent(String.valueOf(embargoDays));
		embargo.appendChild(days_available);
		return embargo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Coverage other = (Coverage) obj;
		return this.from.equals(other.from)
				&& this.to.equals(other.to)
				&& this.embargo.equals(other.embargo);
	}

	@Override
	public int hashCode() {
		int result = from != null ? from.hashCode() : 0;
		result = 31 * result + (to != null ? to.hashCode() : 0);
		result = 31 * result + (embargo != null ? embargo.hashCode() : 0);
		return result;
	}

	@Override
	public int compareTo(Object o) {
		if (this == o || o == null || this.getClass() != o.getClass()) return 0;
		Coverage other = (Coverage) o;
		int intFrom = this.from.isEmpty() ? -1 : Integer.valueOf(this.from);
		int intFromOther = other.from.isEmpty() ? 1 : Integer.valueOf(other.from);
		return Integer.compare(intFrom, intFromOther);
	}
}
