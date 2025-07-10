package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = KramDnntLabel.TABLE_NAME)
public class KramDnntLabel { // licenses

	private static Logger logger = LoggerFactory.getLogger("cz.mzk.recordmanager.server.model.KramDnntLabel");

	public enum DnntLabelEnum {

		DNNTO("dnnto"),
		PAYING_USERS("paying_users");

		private final String label;

		DnntLabelEnum(String label) {
			this.label = label;
		}

		public static List<String> getStringifyLabels() {
			return Arrays.stream(DnntLabelEnum.values()).map(l -> l.label).collect(Collectors.toList());
		}

		public String getLabel() {
			return this.label;
		}
	}

	private static final List<String> AVAILABILITY_PUBLIC = FileUtils.openFile("/list/licenses/online.txt");

	private static final List<String> AVAILABILITY_ONSITE = FileUtils.openFile("/list/licenses/protected.txt");

	private static final List<String> AVAILABILITY_MEMBER = Arrays.asList(
			DnntLabelEnum.PAYING_USERS.getLabel()
	);

	public static final List<String> AVAILABILITY_DNNTO = Arrays.asList(
			DnntLabelEnum.DNNTO.getLabel()
	);

	public static final Map<String, List<String>> AVAILABILITY_MAP = new HashMap<>();

	static {
		AVAILABILITY_MAP.put(Constants.DOCUMENT_AVAILABILITY_ONLINE, AVAILABILITY_PUBLIC);
		AVAILABILITY_MAP.put(Constants.DOCUMENT_AVAILABILITY_PROTECTED, AVAILABILITY_ONSITE);
		AVAILABILITY_MAP.put(Constants.DOCUMENT_AVAILABILITY_MEMBER, AVAILABILITY_MEMBER);
	}

	public static final String TABLE_NAME = "kram_dnnt_label";

	public static final List<String> LABELS = DnntLabelEnum.getStringifyLabels();

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "label")
	private String label;

	public static KramDnntLabel create(String labelName) {
		if (!licenseExists(labelName)) logger.warn("Label '{}' does not exist", labelName);
		KramDnntLabel newKramDnntLabel = new KramDnntLabel();
		newKramDnntLabel.setLabel(labelName);
		return newKramDnntLabel;
	}

	public static boolean licenseExists(String labelName) {
		return AVAILABILITY_PUBLIC.contains(labelName)
				|| AVAILABILITY_ONSITE.contains(labelName)
				|| AVAILABILITY_MEMBER.contains(labelName)
				|| AVAILABILITY_DNNTO.contains(labelName);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "KramDnntLabel{" +
				"label='" + label + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KramDnntLabel that = (KramDnntLabel) o;
		return label.equals(that.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(label);
	}

}