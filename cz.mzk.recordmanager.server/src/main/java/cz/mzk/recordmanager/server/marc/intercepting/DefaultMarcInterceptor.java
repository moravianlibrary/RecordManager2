package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.ItemId;
import cz.mzk.recordmanager.server.model.Sigla;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;
import org.marc4j.marc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultMarcInterceptor implements MarcRecordInterceptor {

	private static Logger logger = LoggerFactory.getLogger(DefaultMarcInterceptor.class);
	private static final String SIGLA_MAP = "item_id_sigla.map";
	private static Mapping SIGLA_MAPPING = null;

	static {
		if (SIGLA_MAPPING == null) {
			try {
				SIGLA_MAPPING = new ResourceMappingResolver(new ClasspathResourceProvider()).resolve(SIGLA_MAP);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Record record;
	private ImportConfiguration conf;
	private String recordId;
	protected static final MarcFactory MARC_FACTORY = new MarcFactoryImpl();
	private static final char ITEM_ID_SUBFIELD_CHAR = 't';

	public DefaultMarcInterceptor(Record record) {
		this.record = record;
	}

	public DefaultMarcInterceptor(Record record, ImportConfiguration conf, String recordId) {
		this.record = record;
		this.conf = conf;
		this.recordId = recordId;
	}

	@Override
	public byte[] intercept() {
		if (conf.getItemId() == null) {
			MarcRecord marcRecord = new MarcRecordImpl(record);
			return marcRecord.export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
		}
		Record newRecord = new RecordImpl();
		newRecord.setLeader(record.getLeader());
		for (ControlField cf : record.getControlFields()) {
			newRecord.addVariableField(cf);
		}

		for (DataField df : record.getDataFields()) {
			processField996(df);
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * add item id to field 996
	 *
	 * @param df {@link DataField}
	 */
	protected void processField996(DataField df) {
		if (!df.getTag().equals("996")) return;
		ItemId itemId = conf.getItemId();
		if(itemId == null) return;
		// remove old subfield
		for (Subfield sf : df.getSubfields(ITEM_ID_SUBFIELD_CHAR)) {
			df.removeSubfield(sf);
		}
		String sigla;
		List<String> getSiglas;
		getSiglas = SIGLA_MAPPING.get(conf.getId().toString());
		if (getSiglas != null && !getSiglas.isEmpty()) {
			sigla = getSiglas.get(0);
		} else {
			List<Sigla> siglas = conf.getSiglas();
			sigla = siglas.isEmpty() ? "" : siglas.get(0).getUniqueId().getSigla();
		}
		Subfield itemIdSubfield = ItemId.getItemIdSubfield(itemId, df, sigla, recordId);
		if (itemIdSubfield == null) logger.debug(String.format("Missing data for itemId: import_confid=%d, 001=%s",
				conf.getId(), record.getControlNumber()));
		else df.addSubfield(itemIdSubfield);

	}

	protected Record getRecord() {
		return this.record;
	}

	protected void setRecord(Record newRecord) {
		this.record = newRecord;
	}

}
