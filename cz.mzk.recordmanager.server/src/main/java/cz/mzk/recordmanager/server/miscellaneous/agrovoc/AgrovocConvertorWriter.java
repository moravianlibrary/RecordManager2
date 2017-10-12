package cz.mzk.recordmanager.server.miscellaneous.agrovoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.marc4j.marc.Record;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;

public class AgrovocConvertorWriter implements ItemWriter<Map<String, Map<String, List<String>>>> {

	@Autowired
	protected SessionFactory sessionFactory;

	private PrintWriter writer;

	private MarcFactoryImpl factory = new MarcFactoryImpl();

	private String filename;

	private static final String PREFLABEL = "prefLabel";
	private static final String ALTLABEL = "altLabel";

	private Map<String, List<String>> prefLabel = new HashMap<>();
	private Map<String, List<String>> altLabel = new HashMap<>();
	private Map<String, Map<String, List<String>>> cache = new HashMap<>();
	{
		cache.put(PREFLABEL, prefLabel);
		cache.put(ALTLABEL, altLabel);
	}

	public AgrovocConvertorWriter(String filename) {
		this.filename = filename;
	}

	@Override
	public void write(List<? extends Map<String, Map<String, List<String>>>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends Map<String, Map<String, List<String>>>> items) throws Exception {
		for (Map<String, Map<String, List<String>>> item : items) {
			for (String key : cache.keySet()) {
				if (item.containsKey(key)) {
					cache.get(key).putAll(item.get(key));
				}
			}
		}
	}

	@AfterStep
	protected void after() {
		try {
			writer = new PrintWriter(filename, "UTF-8");
			for (String key : altLabel.keySet()) {
				if (!prefLabel.containsKey(key)) {
					continue;
				}
				Record record = factory.newRecord();
				record.setLeader(factory.newLeader("-----nz--a22-----n--4500"));
				record.addVariableField(factory.newControlField("001", key));
				record.addVariableField(factory.newDataField("150", ' ', ' ', "a", prefLabel.get(key).get(0)));
				addLabel(record, key, altLabel);
				writer.println((new MarcRecordImpl(record)).export(IOFormat.LINE_MARC));
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	protected Record addLabel(Record record, String key, Map<String, List<String>> map) {
		if (map.containsKey(key)) {
			for (String pref : map.get(key)) {
				record.addVariableField(factory.newDataField("450", ' ', ' ', "a", pref));
			}
		}
		return record;
	}

}
