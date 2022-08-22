package cz.mzk.recordmanager.server.metadata.mappings996;

import cz.mzk.recordmanager.server.util.CaslinLink;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

public class CaslinMappings996 extends DefaultMappings996 {

	@Autowired
	private CaslinLink caslinLink;

	@Override
	public String getDepartment(DataField df) {
		return df.getSubfield('e') != null ? df.getSubfield('e').getData() : "";
	}

	@Override
	public String getCaslinUrl(DataField df) {
		return caslinLink.getCaslinLink(df);
	}

}
