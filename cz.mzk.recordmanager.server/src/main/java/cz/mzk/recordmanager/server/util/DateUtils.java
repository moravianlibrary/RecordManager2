package cz.mzk.recordmanager.server.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtils {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static Date transform(Date date, String zoneId) throws ParseException {
		if (date == null) return null;
		date = DATE_FORMAT.parse(DATE_FORMAT.format(date));
		LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		ZonedDateTime utcZonedDateTime = ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(zoneId));
		return DATE_FORMAT.parse(utcZonedDateTime.toInstant().toString());
	}
}
