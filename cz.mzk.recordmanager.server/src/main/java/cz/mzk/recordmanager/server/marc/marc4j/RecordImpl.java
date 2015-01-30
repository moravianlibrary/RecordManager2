package cz.mzk.recordmanager.server.marc.marc4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * Customized {@link info.freelibrary.marc4j.impl.RecordImpl} implementation,
 * behavior of private method getIterator is changed
 *
 */
public class RecordImpl extends info.freelibrary.marc4j.impl.RecordImpl {

	/**
     * Gets the first {@link VariableField} with the supplied tag.
     * 
     * @param aTag The tag of the field to be returned
     */
    public VariableField getVariableField(String aTag) {
        Iterator<? extends VariableField> iterator = getIterator(aTag);

        while (iterator.hasNext()) {
            VariableField field = iterator.next();

            if (field.getTag().equals(aTag)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s with the supplied tag.
     */
    public List<VariableField> getVariableFields(String aTag) {
        List<VariableField> fields = new ArrayList<VariableField>();
        Iterator<? extends VariableField> iterator = getIterator(aTag);

        while (iterator.hasNext()) {
            VariableField field = iterator.next();

            if (field.getTag().equals(aTag)) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s from the {@link Record}.
     */
    public List<VariableField> getVariableFields() {
        List<VariableField> fields = new ArrayList<VariableField>();
        Iterator<? extends VariableField> iterator = controlFields.iterator();

        while (iterator.hasNext()) {
            fields.add(iterator.next());
        }

        iterator = dataFields.iterator();

        while (iterator.hasNext()) {
            fields.add(iterator.next());
        }

        return fields;
    }


    @SuppressWarnings("unchecked")
	private Iterator<? extends VariableField> getIterator(String aTag) {
        if (aTag.length() == 3) {
            try {
                if (aTag.startsWith("00")) {
                    return controlFields.iterator();
                } else {
                    return dataFields.iterator();
                }
            } catch (final NumberFormatException details) {
                // Log warning below...
            }
        }

        // TODO: log a warning here
        return ((List<VariableField>) Collections.EMPTY_LIST).iterator();
    }
}
