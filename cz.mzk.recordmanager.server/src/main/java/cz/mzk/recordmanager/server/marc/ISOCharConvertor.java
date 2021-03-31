package cz.mzk.recordmanager.server.marc;

import org.marc4j.converter.CharConverter;

import com.google.common.base.CharMatcher;

public class ISOCharConvertor extends CharConverter {

	public static final ISOCharConvertor INSTANCE = new ISOCharConvertor();

	@Override
	public String convert(char[] dataElement) {
		String input = new String(dataElement);
		String fixed = CharMatcher.javaIsoControl().removeFrom(input);
		return fixed;
	}

}
