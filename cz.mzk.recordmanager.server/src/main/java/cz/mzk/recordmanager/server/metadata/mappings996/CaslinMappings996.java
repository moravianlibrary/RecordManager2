package cz.mzk.recordmanager.server.metadata.mappings996;

import cz.mzk.recordmanager.server.model.SiglaCaslin;
import cz.mzk.recordmanager.server.oai.dao.SiglaCaslinDAO;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

public class CaslinMappings996 extends DefaultMappings996 {

	@Autowired
	private SiglaCaslinDAO siglaCaslinDAO;

	private static final Pattern CARMEN = Pattern.compile("carmen", Pattern.CASE_INSENSITIVE);

	@Override
	public String getDepartment(DataField df) {
		return df.getSubfield('e') != null ? df.getSubfield('e').getData() : "";
	}

	@Override
	public String getCaslinUrl(DataField df) {
		if (df.getSubfield('e') == null || df.getSubfield('w') == null) return "";
		SiglaCaslin siglaCaslin = siglaCaslinDAO.getBySigla(df.getSubfield('e').getData());
		if (siglaCaslin == null) return "";
		String id = df.getSubfield('w').getData();
		if (CARMEN.matcher(siglaCaslin.getUrl()).find() && id.length() >= 8) id = id.substring(id.length() - 8);
		return siglaCaslin.getUrl() + id;
	}

}
