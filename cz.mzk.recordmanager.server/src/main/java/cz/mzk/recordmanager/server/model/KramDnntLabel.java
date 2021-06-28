package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = KramDnntLabel.TABLE_NAME)
public class KramDnntLabel {

	public enum DnntLabelEnum {

		DNNTO("dnnto");

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

	public static final String TABLE_NAME = "kram_dnnt_label";

	public static final List<String> LABELS = DnntLabelEnum.getStringifyLabels();

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "label")
	private String label;

	public static KramDnntLabel create(String labelName) {
		if (!LABELS.contains(labelName)) return null;
		KramDnntLabel newKramDnntLabel = new KramDnntLabel();
		newKramDnntLabel.setLabel(labelName);
		return newKramDnntLabel;
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