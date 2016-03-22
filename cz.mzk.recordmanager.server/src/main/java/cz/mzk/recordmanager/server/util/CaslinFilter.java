package cz.mzk.recordmanager.server.util;

import java.util.ArrayList;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.oai.dao.SiglaDAO;

@Component
public class CaslinFilter {

	@Autowired
	private SiglaDAO siglaDao;

	public List<DataField> filter(List<DataField> input){
		List<DataField> result = new ArrayList<DataField>();
		for(DataField df: input){
			Subfield sf = df.getSubfield('e');
			if(sf == null) continue;
			if(siglaDao.findSiglaByName(sf.getData()).isEmpty()){
				result.add(df);
			}
		}
		return result;
	}
}
