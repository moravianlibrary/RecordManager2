package cz.mzk.recordmanager.server.util;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;

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

}
