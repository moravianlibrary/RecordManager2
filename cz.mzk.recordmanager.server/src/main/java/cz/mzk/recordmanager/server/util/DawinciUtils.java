package cz.mzk.recordmanager.server.util;

import com.google.common.primitives.Chars;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

import java.util.StringJoiner;

public class DawinciUtils {

	private static final MarcFactory MARC_FACTORY = new MarcFactoryImpl();

	private static final char[] SF_D_CHARS = {'d', 'y', 'v', 'i'};

	/**
	 * MAPPING
	 * if exists 996 $d, $y, $v or $i => 996 $d = "$d / $y / $v / $i / $p"
	 */
	public static DataField createSubfieldD(DataField df) {
		DataField newDf = MARC_FACTORY.newDataField("996", df.getIndicator1(), df.getIndicator2());
		StringJoiner sfDData = new StringJoiner(" / ");

		for (Subfield sf : df.getSubfields()) {
			if (Chars.contains(SF_D_CHARS, sf.getCode()) && !sf.getData().isEmpty()) {
				sfDData.add(sf.getData());
			}
		}
		if (sfDData.length() == 0) return df;

		Subfield sfP = df.getSubfield('p');
		if (sfP != null && !sfP.getData().isEmpty()) {
			sfDData.add(sfP.getData());
		}
		Subfield sfD = MARC_FACTORY.newSubfield('d', sfDData.toString());
		for (Subfield sf : df.getSubfields()) {
			if (sf.getCode() == 'd') {
				newDf.addSubfield(sfD);
			} else newDf.addSubfield(sf);
		}
		if (df.getSubfield('d') == null) newDf.addSubfield(sfD);
		return newDf;
	}
}
