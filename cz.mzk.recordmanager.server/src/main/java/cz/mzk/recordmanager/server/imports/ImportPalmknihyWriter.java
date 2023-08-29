package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.bibliolinker.keys.DelegatingBiblioLinkerKeysParser;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.hibernate.SessionFactory;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportPalmknihyWriter extends ImportRecordsWriter implements ItemWriter<List<Record>>, StepExecutionListener {

	@Autowired
	protected DelegatingBiblioLinkerKeysParser biblioLinkerKeysParser;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO hrDao;

	private final MarcFactory factory = MarcFactoryImpl.newInstance();

	public ImportPalmknihyWriter(Long configurationId) {
		super(configurationId);
	}

	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	private static final HashMap<Long, Pattern> ID_PARSER = new HashMap<>();

	static {
		ID_PARSER.put(328L, Pattern.compile("CbvkUsCat\\*(.*)"));
		ID_PARSER.put(333L, Pattern.compile("PAK01-(\\d+)"));
		ID_PARSER.put(496L, Pattern.compile("TynUsCat\\*(.*)"));
	}

	private static final HashMap<Long, String> URL = new HashMap<>();

	static {
		URL.put(328L, "https://katalog.cbvk.cz/arl-cbvk/cs/detail-cbvk_us_cat-%s-titul");
		URL.put(333L, "https://aleph.knihovna-pardubice.cz/F/?func=direct&doc_number=%s");
		URL.put(461L, "https://katalog.svkul.cz/detail/%s");
		URL.put(496L, "https://arl4.library.sk/arl-tyn/cs/detail-tyn_us_cat.2-%s-titul/");
		URL.put(498L, "https://orlova.knihovny.net/detail/%s");
		URL.put(499L, "https://pribram.tritius.cz/detail/%s");
		URL.put(501L, "https://tritius.kkvysociny.cz/detail/%s");
		URL.put(502L, "https://tritius.knihovnaprerov.cz/detail/%s");
		URL.put(503L, "https://tritius.knihovnatrinec.cz/detail/%s");
		URL.put(504L, "https://baze.knihovnazn.cz/detail/%s");
		URL.put(505L, "https://brandysnl.tritius.cz/detail/%s");
		URL.put(506L, "https://hodonin.tritius.cz/detail/%s");
		URL.put(507L, "https://knihovnaml.tritius.cz/detail/%s");
		URL.put(508L, "https://kutnahora.tritius.cz/detail/%s");
		URL.put(509L, "https://most.tritius.cz/detail/%s");
		URL.put(510L, "https://tritius.knih-pe.cz/detail/%s");
		URL.put(511L, "https://katalog.mkostrov.cz/detail/%s");
		URL.put(512L, "https://trutnov.tritius.cz/detail/%s");
		URL.put(513L, "https://trutnov.tritius.cz/detail/%s");
		URL.put(514L, "https://tritius.knihovna-cl.cz/detail/%s");
		URL.put(515L, "https://kromeriz.tritius.cz/detail/%s");
		URL.put(516L, "https://tritius.plzen.eu/detail/%s");
		URL.put(517L, "https://klatovy.tritius.cz/detail/%s");
		URL.put(518L, "https://kkkv.tritius.cz/detail/%s");
		URL.put(519L, "https://kolin.tritius.cz/detail/%s");
		URL.put(520L, "https://boskovice.tritius.cz/detail/%s");
		URL.put(521L, "https://chomutovskaknihovna.tritius.cz/detail/%s");
		URL.put(522L, "https://fmi.tritius.cz/detail/%s");
		URL.put(523L, "https://tritius-knihovna.ricany.cz/detail/%s");
		URL.put(524L, "https://trebic.tritius.cz/detail/%s");
		URL.put(525L, "https://jh.tritius.cz/detail/%s");
		URL.put(526L, "https://milovice.tritius.cz/detail/%s");
		URL.put(527L, "https://online.knihovnacaslav.cz/detail/%s");
		URL.put(528L, "https://tritius.knihovnanymburk.cz/detail/%s");
		URL.put(529L, "https://jihlava.tritius.cz/detail/%s");
		URL.put(530L, "https://benesov.tritius.cz/detail/%s");
		URL.put(531L, "https://tritius.kmol.cz/detail/%s");
		URL.put(532L, "https://tritius.knihovnachodov.cz/detail/%s");
		URL.put(533L, "https://kmhk.tritius.cz/detail/%s");
		URL.put(534L, "https://tritius.knih-pi.cz/detail/%s");
	}


	protected void writeInner(List<? extends List<Record>> items) {
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				Long isbn = null;
				DataField field856 = null;
				String vyp = "0";
				String url_id = null;
				for (DataField df : currentRecord.getDataFields()) {
					if (df.getTag().equals("020") && df.getSubfield('a') != null) {
						isbn = ISBNUtils.toISBN13Long(df.getSubfield('a').getData());
					}
					if (df.getTag().equals("856")) field856 = df;
					if (df.getTag().equals("VYP")) vyp = df.getSubfield('a').getData();
					if (df.getTag().equals("URL")) url_id = df.getSubfield('a').getData();
				}
				if (field856 == null || !vyp.equals("1") || url_id == null) continue;
				for (byte[] rawRecord : hrDao.getMetadataForPalmknihy(isbn, url_id)) {
					Record palmknihy = marcXmlParser.parseUnderlyingRecord(rawRecord);
					palmknihy.addVariableField(field856);
					boolean eversionExists = false;
					for (HarvestedRecord hrLib : hrDao.getByPalmknihyId(url_id)) {
						String record_id = hrLib.getUniqueId().getRecordId();
						if (ID_PARSER.containsKey(hrLib.getHarvestedFrom().getId())) {
							Matcher matcher = ID_PARSER.get(hrLib.getHarvestedFrom().getId())
									.matcher(hrLib.getUniqueId().getRecordId());
							if (matcher.matches()) {
								record_id = matcher.group(1);
							}
						}
						if (!URL.containsKey(hrLib.getHarvestedFrom().getId())) continue;
						palmknihy.addVariableField(factory.newDataField("856", ' ', ' ', "u",
								String.format(URL.get(hrLib.getHarvestedFrom().getId()), record_id),
								"z", hrLib.getHarvestedFrom().getIdPrefix()));
						eversionExists = true;
					}
					if (!eversionExists) continue;
					palmknihy.addVariableField(factory.newDataField("OAI", ' ', ' ', "a", currentRecord.getControlNumber()));
					processRecord(palmknihy);
					break;
				}
			}
		}
	}

}
