package cz.mzk.recordmanager.server.scripting.function;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import cz.mzk.recordmanager.server.AbstractTest;

public class PublishDateMarcFunctionsTest extends AbstractTest {

	@Autowired
	private PublishDateMarcFunctions functions;

	@Test
	public void parseRangesSingleYear() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("2005"));
		Assert.assertEquals(result, Sets.newHashSet(2005));
	}

	@Test
	public void parseFullRangeWithDash() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("1910-1913"));
		Assert.assertEquals(result, Sets.newHashSet(1910, 1911, 1912, 1913));
	}

	@Test
	public void parseFullRangeWithSlash() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("1910/1913"));
		Assert.assertEquals(result, Sets.newHashSet(1910, 1911, 1912, 1913));
	}

	@Test
	public void parseShortenRangeWithDash() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("1950-53"));
		Assert.assertEquals(result, Sets.newHashSet(1950, 1951, 1952, 1953));
	}

	@Test
	public void parseShortenRangeWithSlash() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("1950/53"));
		Assert.assertEquals(result, Sets.newHashSet(1950, 1951, 1952, 1953));
	}

	@Test
	public void parseListRange() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("2001,2002,2003,2004"));
		Assert.assertEquals(result, Sets.newHashSet(2001, 2002, 2003, 2004));
	}

	@Test
	public void parseListRangeWithEndingComma() {
		Set<Integer> result = functions.parseRanges(Collections.singletonList("1945,1946,1947,1948,"));
		Assert.assertEquals(result, Sets.newHashSet(1945, 1946, 1947, 1948));
	}

	@Test
	public void parseField008SingleYear() {
		String input = "140526s2014----xr-----e-l----001-0-cze--";
		Set<Integer> result = functions.parsePublishDateFrom008(input);
		Assert.assertEquals(result, Sets.newHashSet(2014));
	}

	@Test
	public void parseField008UncertainRange() {
		String input = "130902c19uu9999au-ac---------000-0-ger--";
		Set<Integer> result = functions.parsePublishDateFrom008(input);
		Assert.assertEquals(result.size(), 121);
	}

}
