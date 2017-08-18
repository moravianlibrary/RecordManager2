package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.google.common.primitives.Chars;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;


public class NlkMarcInterceptor extends DefaultMarcInterceptor{

	public NlkMarcInterceptor(Record record){
		super(record);
	}
	
	@Override
	public byte[] intercept(){
		if(super.getRecord() == null){
			return new byte[0];
		}
		
		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();
		
		MarcFactory marcFactory = new MarcFactoryImpl();
		
		newRecord.setLeader(getRecord().getLeader());
		for(ControlField cf: super.getRecord().getControlFields()){
			newRecord.addVariableField(cf);
		}
		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for(String tag: new TreeSet<String>(dfMap.keySet())){ // sorted tags
			for(DataField df: dfMap.get(tag)){				
				if(df.getTag().equals("996")){
					/*
					 * MAPPING
					 * if exists 996 $d, $y, $v or $i => 996 $d = "$d / $y / $v / $i / $p"
					 */
					
					DataField newDf = marcFactory.newDataField();
					newDf.setTag(tag);
					newDf.setIndicator1(df.getIndicator1());
					newDf.setIndicator2(df.getIndicator2());

					StringJoiner sj = new StringJoiner(" / ");
					for(Subfield sf: df.getSubfields()){
						if(Chars.contains(new char[]{'d','y','v','i'}, sf.getCode())){
							if(!sf.getData().isEmpty()){
								sj.add(sf.getData());
							}
						}
					}
					if(sj.length() > 0){
						Subfield sfp = df.getSubfield('p');
						if(!sfp.getData().isEmpty()){
							sj.add(sfp.getData());
						}
						for(Subfield sf: df.getSubfields()){
							if(sf.getCode() == 'd'){
								Subfield newSf = marcFactory.newSubfield('d');
								newSf.setData(sj.toString());
								newDf.addSubfield(newSf);
							}
							else newDf.addSubfield(sf);
						}
					}
					else newDf = df;
					processField996(newDf);
					newRecord.addVariableField(newDf);
				}
				else if (df.getTag().equals("990") || df.getTag().equals("991")) {
					continue;
				}
				else {
					newRecord.addVariableField(df);
				}
			}
		}
		
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
