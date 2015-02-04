package cz.mzk.recordmanager.server.oai.harvest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OAIHarvesterUtils {
	
	public static final String HARVEST_DATE_FORMAT1 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String HARVEST_DATE_FORMAT2 = "yyyy-MM-dd";
		
	public static Date stringToDate(final String strDate) {
		try {
			return stringToDateExp(strDate);
		} catch (ParseException e) {}
		return null;
	}
	
	private static Date stringToDateExp(final String strDate) throws ParseException {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(HARVEST_DATE_FORMAT1);
			return sdf.parse(strDate);
		} catch (ParseException e) {}
		SimpleDateFormat sdf = new SimpleDateFormat(HARVEST_DATE_FORMAT2);
		return sdf.parse(strDate);
	}
}
