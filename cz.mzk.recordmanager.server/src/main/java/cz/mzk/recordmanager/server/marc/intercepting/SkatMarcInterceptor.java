package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.util.CaslinFilter;

public class SkatMarcInterceptor extends DefaultMarcInterceptor {
	
	@Autowired
	private CaslinFilter cf;
	
	public SkatMarcInterceptor(Record record) {
		super(record);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}
		
		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();
		
		MarcFactory marcFactory = new MarcFactoryImpl();
		
		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf: super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for (String tag: new TreeSet<String>(dfMap.keySet())) {
			for (DataField df: dfMap.get(tag)) {
				if (df.getTag().equals("910")) {
					/*
					 * MAPPING
					 * 910 a = 996e
					 * 910 b = 996c
					 * 910 r + s  = 996 d
					 * 910x = 996w
					 * 910p = 996p 
					 */
					
					List<Pair<Character,Character>> directMapping = new ArrayList<>();
					directMapping.add(Pair.of('a','e'));
					directMapping.add(Pair.of('b','c'));
					directMapping.add(Pair.of('x','w'));
					directMapping.add(Pair.of('p','p'));
			
					DataField newDf = marcFactory.newDataField("996", ' ', ' ');
					for (Pair<Character,Character> mapping: directMapping) {
						if (df.getSubfields(mapping.getLeft()).isEmpty()) {
							continue;
						}
						newDf.addSubfield(
								marcFactory.newSubfield(
										mapping.getRight(),
										df.getSubfields(mapping.getLeft()).stream().map(Subfield::getData).collect(Collectors.joining(","))
										)
								);
					}
					String joinedContent = "";
					if (df.getSubfield('r') != null) {
						joinedContent += df.getSubfield('r').getData();
					}
					if (df.getSubfield('s') != null) {
						joinedContent += df.getSubfield('s').getData();
					}
					if (!joinedContent.isEmpty()) {
						newDf.addSubfield(
								marcFactory.newSubfield('d', joinedContent));
					}
					if (df.getSubfield('a') != null && cf.filter(df.getSubfield('a').getData())) {
						newDf.addSubfield(marcFactory.newSubfield('q', "0"));
					}
					newDf.addSubfield(marcFactory.newSubfield('s', "NZ"));
					newRecord.addVariableField(newDf);
				} else if (df.getTag().equals("996")) {
					if ((df.getSubfield('e') != null) && (cf.filter(df.getSubfield('e').getData()))
							&& (df.getSubfield('q') == null || !df.getSubfield('q').getData().equals("0"))) {
						df.addSubfield(marcFactory.newSubfield('q', "0"));
					}
					newRecord.addVariableField(df);
				} else {
					newRecord.addVariableField(df);
				}
			}
		}

		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
	
}
