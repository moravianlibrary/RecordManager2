package cz.mzk.recordmanager.server.marc.intercepting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;

public class MzkNormsMarcInterceptor extends DefaultMarcInterceptor{

	public MzkNormsMarcInterceptor(Record record){
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
				// kill fields 996, 910 and 540
				if(df.getTag().equals("996")) continue;
				if(df.getTag().equals("910")) continue;
				if(df.getTag().equals("540")){
					if(df.getSubfield('a').getData().contains("Normy lze objednat u pultu ve Studovně novin "
							+ "a časopisů (2.p.) a studovat se mohou pouze ve studovně.")) continue;
				}
				
				if(df.getTag().equals("520")){
					/*
					 * MAPPING
					 * 520 a Norma je platná = 500a
					 */
					
					List<Pair<Character,Character>> directMapping = new ArrayList<>();
					directMapping.add(Pair.of('a','a'));
			
					DataField newDf = marcFactory.newDataField("500", ' ', ' ');
					for(Pair<Character,Character> mapping: directMapping){
						Subfield sf = df.getSubfield(mapping.getLeft());
						if(!sf.getData().contains("Norma je platná")){
							continue;
						}
						newDf.addSubfield(sf);
					}

					newRecord.addVariableField(newDf);
				}
				else{
					newRecord.addVariableField(df);
				}
			}
		}
		
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes();
	}

}
