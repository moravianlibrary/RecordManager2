package cz.mzk.recordmanager.server.marc.marc4j;

import org.marc4j.MarcException;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/**
 * customized implementation of
 * {@link info.freelibrary.marc4j.impl.MarcFactoryImpl}. Custom {@link Record}
 * and {@link DataField} implementations are used.
 */
public class MarcFactoryImpl extends
		info.freelibrary.marc4j.impl.MarcFactoryImpl {

	public MarcFactoryImpl() {
	}

	public static MarcFactory newInstance() {
		return new MarcFactoryImpl();
	}

	/**
	 * Returns a new data field instance.
	 *
	 * @return DataField
	 */
	@Override
	public DataField newDataField() {
		return new DataFieldImpl();
	}

	/**
	 * Creates a new data field with the given tag and indicators and returns
	 * the instance.
	 *
	 * @return DataField
	 */
	@Override
	public DataField newDataField(String tag, char ind1, char ind2) {
		return new DataFieldImpl(tag, ind1, ind2);
	}

	/**
	 * Creates a new data field with the given tag and indicators and subfields
	 * and returns the instance.
	 *
	 * @return DataField
	 */
	@Override
	public DataField newDataField(String tag, char ind1, char ind2,
			String... subfieldCodesAndData) {
		DataField df = new DataFieldImpl(tag, ind1, ind2);
		if (subfieldCodesAndData.length % 2 == 1) {
			throw new MarcException(
					"Error: must provide even number of parameters for subfields: code, data, code, data, ...");
		}
		for (int i = 0; i < subfieldCodesAndData.length; i += 2) {
			if (subfieldCodesAndData[i].length() != 1) {
				throw new MarcException(
						"Error: subfieldCode must be a single character");
			}
			Subfield sf = 
					newSubfield(subfieldCodesAndData[i].charAt(0),
							subfieldCodesAndData[i + 1]);
			df.addSubfield(sf);
		}
		return (df);
	}

	/**
	 * Returns a new control field instance.
	 *
	 * @return ControlField
	 */
	@Override
	public ControlField newControlField() {
		return new AlphaNumericControlFieldImpl();
	}

	/**
	 * Creates a new control field with the given tag and returns the instance.
	 *
	 * @return ControlField
	 */
	@Override
	public ControlField newControlField(String tag) {
		return new AlphaNumericControlFieldImpl(tag);
	}

	/**
	 * Creates a new control field with the given tag and data and returns the
	 * instance.
	 *
	 * @return ControlField
	 */
	@Override
	public ControlField newControlField(String tag, String data) {
		return new AlphaNumericControlFieldImpl(tag, data);
	}

	/**
	 * Returns a new {@link Record} with the supplied {@link Leader}.
	 */
	@Override
	public Record newRecord(Leader leader) {
		Record record = new RecordImpl();
		record.setLeader(leader);
		return record;
	}

}
