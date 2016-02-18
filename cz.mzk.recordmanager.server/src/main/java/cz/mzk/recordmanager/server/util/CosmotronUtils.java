package cz.mzk.recordmanager.server.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class CosmotronUtils {
	
	public static String get77308w(MarcRecord mr){
		for(DataField df: mr.getDataFields("773")){
			if(df.getIndicator1() == '0' && df.getIndicator2() == '8'){
				if(df.getSubfield('w') != null)	return parseIdFrom773(df.getSubfield('w').getData());
			}
			if(df.getIndicator1() == '0'){
				if(df.getSubfield('7') != null && df.getSubfield('7').getData().equals("nnas")){
					if(df.getSubfield('w') != null)	return parseIdFrom773(df.getSubfield('w').getData());
				}
			}
		}
		return null;
	}
	
	public static String parseIdFrom773(String s){
		s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
		s = s.replaceAll("_us_cat\\*", "UsCat"+Constants.COSMOTRON_RECORD_ID_CHAR);
		return s;
	}
	
	public static HarvestedRecord update996(HarvestedRecord hr, Cosmotron996 new996){
		List<Cosmotron996> result = new ArrayList<Cosmotron996>();
		boolean exists = false;
		for(Cosmotron996 c996: hr.getCosmotron()){
			if(c996.getRecordId().equals(new996.getRecordId())){
				c996.setUpdated(new Date());
				if(new996.getDeleted() != null){
					c996.setDeleted(new Date());
					c996.setRawRecord(new byte[0]);
				}
				else{
					c996.setRawRecord(new996.getRawRecord());					
				}
				exists = true;				
			}
			
			result.add(c996);
		}
		if(!exists) result.add(new996);
		
		hr.setUpdated(new Date());
		hr.setCosmotron(result);
		
		return hr;
	}
}
