package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class OpenlibMarcInterceptor extends DefaultMarcInterceptor{

	private static final String TEXT_856y = "Elektronická výpůjčka přes OpenLibrary (nutná osobní registrace)";
	
	private static final Pattern OPENLIBRARY_URL = Pattern.compile("http://www.openlibrary.org.*");
	
	public OpenlibMarcInterceptor(Record record) {
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
				if(df.getTag().equals("856")){
					// kill field 85641
					if(df.getIndicator1()=='4' && df.getIndicator2()=='1') continue;
					if(df.getIndicator1()=='4' && df.getIndicator2()=='2'){
						Subfield sf = df.getSubfield('u');
						if(sf != null){
							Matcher matcher = OPENLIBRARY_URL.matcher(sf.getData());
							if(matcher.matches()){
								DataField newDf = marcFactory.newDataField("856", '4', '2');
								newDf.addSubfield(df.getSubfield('u'));
								newDf.addSubfield(marcFactory.newSubfield('y', TEXT_856y));
								newRecord.addVariableField(newDf);
							}
						}
					}
				}
				else newRecord.addVariableField(df);
			}
		}
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
}
