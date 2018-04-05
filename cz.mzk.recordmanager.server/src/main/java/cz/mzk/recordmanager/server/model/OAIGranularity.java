package cz.mzk.recordmanager.server.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum OAIGranularity {
	DAY ("YYYY-MM-DD", "yyyy-MM-dd"),
	SECOND ("YYYY-MM-DDTHH:MM:SSZ", "yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private final String readableFormat;
	private final String parseableFormat;
	
	OAIGranularity(String readableFormat, String parsableFormat) {
		this.readableFormat = readableFormat;
		this.parseableFormat = parsableFormat;
	}
	
	public String getReadableFormat() {
		return readableFormat;
	}
	
	public String getparseableFormat() {
		return parseableFormat;
	}
	
	/**
	 * 
	 * @param date {@link Date}
	 * @return {@link String} representation of given day.  
	 */
	public String dateToString(final Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(this.getparseableFormat());
		return sdf.format(date);
	}
	
	/**
	 * 
	 * @param strGranularity human readable format of granularity
	 * @return {@link OAIGranularity} object corresponding to given string or null
	 */
	public static OAIGranularity stringToOAIGranularity(final String strGranularity) {
		for (OAIGranularity gran: OAIGranularity.values()) {
			if (gran.getReadableFormat().equalsIgnoreCase(strGranularity)) {
				return gran;
			}
		}
		return null;
	}
	
	/**
	 * convert given string representing date to {@link Date} object using 
	 * most precise granularity available 
	 * @param strDate String representation of Date
	 * @return Date if string can be converted, null otherwise
	 */
	public static Date stringToDate(final String strDate) {
		//order matters
		for (OAIGranularity gran: new OAIGranularity[]{SECOND, DAY}) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(gran.getparseableFormat());
				return sdf.parse(strDate);
			} catch (ParseException e) {}
		}
		return null;
	}

}
