package cz.mzk.recordmanager.server.marc.marc4j;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;


/**
 * customized implementation of {@link info.freelibrary.marc4j.impl.MarcFactoryImpl}.
 * Custom {@link Record} and {@link DataField} implementations are used.
 */
public class MarcFactoryImpl extends info.freelibrary.marc4j.impl.MarcFactoryImpl {

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
    public DataField newDataField() {
        return new DataFieldImpl();
    }

    /**
     * Creates a new data field with the given tag and indicators and returns
     * the instance.
     * 
     * @return DataField
     */
    public DataField newDataField(String tag, char ind1, char ind2) {
        return new DataFieldImpl(tag, ind1, ind2);
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
