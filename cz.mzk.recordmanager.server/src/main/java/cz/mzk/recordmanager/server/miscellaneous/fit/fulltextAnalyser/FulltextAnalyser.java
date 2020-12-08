package cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser;

import java.util.ArrayList;
import java.util.List;

public class FulltextAnalyser {

	private String mzkId;
	private String nkpId;
	private String obalkyKnihId;
	private List<String> names;

	public String getMzkId() {
		return mzkId;
	}

	public void setMzkId(String mzkId) {
		this.mzkId = mzkId;
	}

	public String getNkpId() {
		return nkpId;
	}

	public void setNkpId(String nkpId) {
		this.nkpId = nkpId;
	}

	public String getObalkyKnihId() {
		return obalkyKnihId;
	}

	public void setObalkyKnihId(String obalkyKnihId) {
		this.obalkyKnihId = obalkyKnihId;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public void addName(String name) {
		if (this.names == null) this.names = new ArrayList<>();
		this.names.add(name);
	}

	@Override
	public String toString() {
		return "FulltextAnalyser{" +
				"mzkId='" + mzkId + '\'' +
				", nkpId='" + nkpId + '\'' +
				", obalkyKnihId='" + obalkyKnihId + '\'' +
				", names=" + names +
				'}';
	}
}
