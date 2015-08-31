package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.MarcScriptFactory;

public class MarcScriptFactoryTest extends AbstractTest {

	@Autowired
	private MarcXmlParser parser;

	@Autowired
	private MarcScriptFactory factory;

	@Test
	@SuppressWarnings("unchecked")
	public void test1() {
		MarcRecord record = parser.parseRecord(this.getClass()
				.getResourceAsStream("/records/marcxml/MZK01-001439241.xml"));
		InputStream is1 = getClass().getResourceAsStream(
				"/groovy/ExtendedMarc.groovy");
		InputStream is2 = getClass().getResourceAsStream(
				"/groovy/BaseMarc.groovy");
		MappingScript<MarcFunctionContext> script = factory.create(is1, is2);
		MarcFunctionContext ctx = new MarcFunctionContext(record);
		Map<String, Object> entries = script.parse(ctx);
		Assert.assertEquals(entries.get("author"), null);
		Assert.assertEquals(entries.get("published"), "Rožnov pod Radhoštěm : Proxima Bohemia, 2014");
		Assert.assertEquals(entries.get("title"), "Česká republika : města a obce České republiky : tradice, historie, památky, turistika, současnost /");
		Assert.assertNotNull(entries.get("language"));
		Assert.assertTrue(entries.get("language") instanceof List<?>);
		List<String> languages = (List<String>) entries.get("language");
		Assert.assertEquals(languages.size(), 1);
		Assert.assertEquals(languages.get(0), "Czech");
	}

	@Test
	public void test2() {
		MarcRecord record = parser.parseRecord(this.getClass()
				.getResourceAsStream("/records/marcxml/MZK01-000087310.xml"));
		InputStream is1 = getClass().getResourceAsStream(
				"/groovy/ExtendedMarc.groovy");
		InputStream is2 = getClass().getResourceAsStream(
				"/groovy/BaseMarc.groovy");
		MappingScript<MarcFunctionContext> script = factory.create(is1, is2);
		MarcFunctionContext ctx = new MarcFunctionContext(record);
		Map<String, Object> entries = script.parse(ctx);
		Collection<?> author2Roles = (Collection<?>) entries.get("author2_role");
		Assert.assertTrue(author2Roles.isEmpty());
	}

}
