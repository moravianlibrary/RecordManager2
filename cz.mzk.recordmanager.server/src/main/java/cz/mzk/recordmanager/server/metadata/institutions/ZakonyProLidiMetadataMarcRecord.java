package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

public class ZakonyProLidiMetadataMarcRecord extends MetadataMarcRecord{
	
	private static final String TEXT_NARIZENI_VLADY = "Narizeni_vlady";
	private static final String TEXT_VYHLASKA = "Vyhlaska";
	private static final String TEXT_SDELENI = "Sdeleni";
	private static final String TEXT_ZAKON = "Zakon";
	private static final String TEXT_NALEZ = "Nalez";
	private static final String TEXT_UZ = "Ustava";
	private static final String TEXT_ROZHODNUTI = "Rozhodnuti";
	private static final String TEXT_LAWS_TEXT = "Uplne_zneni";
	private static final String TEXT_DEKRET = "Dekret";
	private static final String TEXT_USNESENI = "Usneseni";
	private static final String TEXT_OPATRENI = "Opatreni";
	private static final String TEXT_SMERNICE = "Smernice";
	private static final String TEXT_SMLOUVA = "Smlouva";
	private static final String TEXT_REDAKCNI_SDELENI = "Redakcni_oznameni";
	private static final String TEXT_PRAVIDLA = "Pravidla";
	private static final String TEXT_STANOVY = "Stanovy";
	private static final String TEXT_UMLUVA = "Umluva";
	private static final String TEXT_ZASADY = "Zasady";
	private static final String TEXT_DOHODA = "Dohoda";
	private static final String TEXT_POKYNY = "Pokyny";
	private static final String TEXT_NARIZENI = "Narizeni";
	private static final String TEXT_OPATRENI_SENATU = "Zakonne_opatreni_senatu";
	private static final String TEXT_RAMCOVE_PODM = "Ramcove_podminky";
	private static final String TEXT_OZNAMENI = "Oznameni";
	private static final String TEXT_OSTATNI = "Ostatni";
	private static final String TEXT_VYNOS = "Vynos";
	private static final String TEXT_ROZKAZ = "Rozkaz";
	private static final String TEXT_POSTUP = "Postup";
	private static final String TEXT_RAD = "Rad";
	
	private static final String TEXT_UCINOST_DO = "Účinnost do";
	
	private static final HashMap<String, HarvestedRecordFormatEnum> TYPES;
    static
	{
    	TYPES = new HashMap<>();
    	TYPES.put(TEXT_NARIZENI_VLADY, HarvestedRecordFormatEnum.LEGISLATIVE_GOVERNMENT_ORDERS);
    	TYPES.put(TEXT_VYHLASKA, HarvestedRecordFormatEnum.LEGISLATIVE_ORDINANCES);
    	TYPES.put(TEXT_SDELENI, HarvestedRecordFormatEnum.LEGISLATIVE_COMMUNICATION);
    	TYPES.put(TEXT_UZ, HarvestedRecordFormatEnum.LEGISLATIVE_CONSTITUTIONAL_LAWS);
    	TYPES.put(TEXT_ZAKON, HarvestedRecordFormatEnum.LEGISLATIVE_LAWS);
    	TYPES.put(TEXT_NALEZ, HarvestedRecordFormatEnum.LEGISLATIVE_FINDING);
    	TYPES.put(TEXT_ROZHODNUTI, HarvestedRecordFormatEnum.LEGISLATIVE_DECISIONS);
    	TYPES.put(TEXT_LAWS_TEXT, HarvestedRecordFormatEnum.LEGISLATIVE_LAWS_TEXT);
    	TYPES.put(TEXT_DEKRET, HarvestedRecordFormatEnum.LEGISLATIVE_DECREES);
    	TYPES.put(TEXT_USNESENI, HarvestedRecordFormatEnum.LEGISLATIVE_RESOLUTIONS);
    	TYPES.put(TEXT_OPATRENI, HarvestedRecordFormatEnum.LEGISLATIVE_MEASURES);
    	TYPES.put(TEXT_SMERNICE, HarvestedRecordFormatEnum.LEGISLATIVE_DIRECTIVES);
    	TYPES.put(TEXT_SMLOUVA, HarvestedRecordFormatEnum.LEGISLATIVE_TREATIES);
    	TYPES.put(TEXT_REDAKCNI_SDELENI, HarvestedRecordFormatEnum.LEGISLATIVE_EDITORIAL);
    	TYPES.put(TEXT_PRAVIDLA, HarvestedRecordFormatEnum.LEGISLATIVE_RULES);
    	TYPES.put(TEXT_STANOVY, HarvestedRecordFormatEnum.LEGISLATIVE_STATUTES);
    	TYPES.put(TEXT_UMLUVA, HarvestedRecordFormatEnum.LEGISLATIVE_CONVENTIONS);
    	TYPES.put(TEXT_ZASADY, HarvestedRecordFormatEnum.LEGISLATIVE_PRINCIPLES);
    	TYPES.put(TEXT_DOHODA, HarvestedRecordFormatEnum.LEGISLATIVE_AGREEMENTS);
    	TYPES.put(TEXT_POKYNY, HarvestedRecordFormatEnum.LEGISLATIVE_GUIDELINES);
    	TYPES.put(TEXT_NARIZENI, HarvestedRecordFormatEnum.LEGISLATIVE_REGULATIONS);
    	TYPES.put(TEXT_OPATRENI_SENATU, HarvestedRecordFormatEnum.LEGISLATIVE_SENATE_MEASURES);
    	TYPES.put(TEXT_RAMCOVE_PODM, HarvestedRecordFormatEnum.LEGISLATIVE_CONDITIONS);
    	TYPES.put(TEXT_OZNAMENI, HarvestedRecordFormatEnum.LEGISLATIVE_NOTICE);
    	TYPES.put(TEXT_VYNOS, HarvestedRecordFormatEnum.LEGISLATIVE_EDICTS);
    	TYPES.put(TEXT_ROZKAZ, HarvestedRecordFormatEnum.LEGISLATIVE_ORDERS);
    	TYPES.put(TEXT_POSTUP, HarvestedRecordFormatEnum.LEGISLATIVE_PROCEDURES);
    	TYPES.put(TEXT_RAD, HarvestedRecordFormatEnum.LEGISLATIVE_CODE);
    	TYPES.put(TEXT_OSTATNI, HarvestedRecordFormatEnum.LEGISLATIVE_OTHERS);
    }
	
	public ZakonyProLidiMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		for(DataField df: underlayingMarc.getDataFields("653")){
			if(df.getSubfield('a') != null){
				String key = df.getSubfield('a').getData();
				if(TYPES.containsKey(key)) return Collections.singletonList(TYPES.get(key));
			}
		}
		return Collections.singletonList(HarvestedRecordFormatEnum.LEGISLATIVE_OTHERS);		
	}
	
	@Override
	public boolean matchFilter(){
		for(DataField df: underlayingMarc.getDataFields("500")){
			if(df.getSubfield('a') != null){
				if(df.getSubfield('a').getData().startsWith(TEXT_UCINOST_DO)) {
					return false;
				}
			}
		}
		return true;
	}
}
