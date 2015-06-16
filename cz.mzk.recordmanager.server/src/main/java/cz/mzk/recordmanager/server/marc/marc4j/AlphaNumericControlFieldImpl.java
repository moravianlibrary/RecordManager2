package cz.mzk.recordmanager.server.marc.marc4j;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.Constants;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.InvalidMARCException;

import com.google.common.collect.ImmutableSet;

import info.freelibrary.marc4j.impl.VariableFieldImpl;

public class AlphaNumericControlFieldImpl extends VariableFieldImpl implements ControlField {

    private static final long serialVersionUID = 8049827626175226331L;

    private String myData;

    private static final Set<String> RESERVED_CONTROL_FIELDS = ImmutableSet.of("FMT", "LDR");

    /**
     * Creates a new <code>ControlField</code>.
     */
    public AlphaNumericControlFieldImpl() {
    }

    /**
     * Creates a new <code>ControlField</code> and sets the tag name.
     *
     * @param aTag The field tag for the <code>ControlField</code>
     */
    public AlphaNumericControlFieldImpl(String aTag) {
        setTag(aTag);
    }

    /**
     * Creates a new <code>ControlField</code> and sets the tag name and the
     * data element.
     *
     * @param aTag The tag for the <code>ControlField</code>
     * @param aData The data for the <code>ControlField</code>
     */
    public AlphaNumericControlFieldImpl(String aTag, String aData) {
        setTag(aTag);
        setData(aData);
    }

    /**
     * Sets the tag of a <code>ControlField</code>.
     *
     * @param aTag The tag of a <code>ControlField</code>
     */
    public void setTag(String aTag) {
    	if (!Constants.CF_TAG_PATTERN.matcher(aTag).find() && !RESERVED_CONTROL_FIELDS.contains(aTag)) {
            throw new InvalidMARCException(aTag +
                    " is not a valid ControlField tag");
        }
        super.setTag(aTag);
    }

    /**
     * Sets the {@link ControlField} data.
     *
     * @param aData The data for the <code>ControlField</code>
     */
    public void setData(String aData) {
        myData = aData;
    }

    /**
     * Gets the {@link ControlField} data.
     *
     * @return The <code>ControlField</code>'s data
     */
    public String getData() {
        return myData;
    }

    /**
     * Returns a string representation of this control field.
     * <p>
     * For example:
     *
     * <pre>
     *     001 12883376
     * </pre>
     * </p>
     *
     * @return A string representation of this control field
     */
    public String toString() {
        return super.toString() + " " + getData();
    }

    /**
     * Finds a match to a regular expression pattern in the {@link ControlField}
     * 's data.
     *
     * @param aPattern The regular expression pattern to compare against the
     *        <code>ControlField</code>'s data
     */
    public boolean find(String aPattern) {
        Pattern pattern = Pattern.compile(aPattern);
        Matcher matcher = pattern.matcher(getData());

        return matcher.find();
    }

}
