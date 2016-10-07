package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class ZakonyProLidiMetadataMarcRecord extends MetadataMarcRecord{

	private static final String TEXT_NARIZENI = "Nařízení vlády"; 
	private static final String TEXT_VYHLASKA = "Vyhláška";
	private static final String TEXT_SDELENI = "Sdělení";
	private static final String TEXT_ZAKON = "Zákon";
	private static final String TEXT_NALEZ = "Nález Ústavního soudu";
	private static final String TEXT_UZ = "Ústavní zákon";
	private static final String TEXT_ROZHODNUTI = "Rozhodnutí";
	private static final String TEXT_LAWS_TEXT = "Úplné znění zákona";
	
	private static final HashMap<String, HarvestedRecordFormatEnum> TYPES = new HashMap<>();
    {
    	TYPES.put(TEXT_NARIZENI, HarvestedRecordFormatEnum.LEGISLATIVE_GOVERNMENT_ORDERS);
    	TYPES.put(TEXT_VYHLASKA, HarvestedRecordFormatEnum.LEGISLATIVE_REGULATIONS);
    	TYPES.put(TEXT_SDELENI, HarvestedRecordFormatEnum.LEGISLATIVE_COMMUNICATION);
    	TYPES.put(TEXT_ZAKON, HarvestedRecordFormatEnum.LEGISLATIVE_LAWS);
    	TYPES.put(TEXT_NALEZ, HarvestedRecordFormatEnum.LEGISLATIVE_FINDING);
    	TYPES.put(TEXT_UZ, HarvestedRecordFormatEnum.LEGISLATIVE_CONSTITUTIONAL_LAWS);
    	TYPES.put(TEXT_ROZHODNUTI, HarvestedRecordFormatEnum.LEGISLATIVE_DECISIONS);
    	TYPES.put(TEXT_LAWS_TEXT, HarvestedRecordFormatEnum.LEGISLATIVE_LAWS_TEXT);
    }
	
	public ZakonyProLidiMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> result = new ArrayList<>();
		for(DataField df: underlayingMarc.getDataFields("245")){
			if(df.getSubfield('a') != null){
				String data = df.getSubfield('a').getData();
				for(String type_str: TYPES.keySet()){
					if(data.startsWith(type_str)){
						result.add(TYPES.get(type_str));
						return result;
					}
				}
			}
		}
		return result;		
	}
}
