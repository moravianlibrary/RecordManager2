package cz.mzk.recordmanager.server.util;

import org.apache.commons.validator.routines.UrlValidator;

public class UrlValidatorUtils {

	public static UrlValidator doubleSlashUrlValidator(){
		return new UrlValidator(UrlValidator.ALLOW_2_SLASHES);
	}

}
