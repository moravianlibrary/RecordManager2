package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;

public class KkvyNormsMarcInterceptor extends DefaultMarcInterceptor{

	public KkvyNormsMarcInterceptor(Record record){
		super(record);
	}
	
	@Override
	public byte[] intercept(){
		if(super.getRecord() == null){
			return new byte[0];
		}
		
		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();
		
		newRecord.setLeader(getRecord().getLeader());
		for(ControlField cf: super.getRecord().getControlFields()){
			newRecord.addVariableField(cf);
		}
		
		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for(String tag: new TreeSet<String>(dfMap.keySet())){ // sorted tags
			for(DataField df: dfMap.get(tag)){
				// kill fields 996l = VF
				if (df.getTag().equals("996")) {
					if (df.getSubfield('l') != null && df.getSubfield('l').getData().trim().equals("VF")) continue;
				}
				processField996(df);
				newRecord.addVariableField(df);
			}
		}
		
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
