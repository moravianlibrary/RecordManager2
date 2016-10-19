package cz.mzk.recordmanager.server.marc.marc4j;

import info.freelibrary.marc4j.impl.VariableFieldImpl;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.Constants;
import org.marc4j.marc.ControlField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class AlphaNumericControlFieldImpl extends VariableFieldImpl implements ControlField {

	private static Logger logger = LoggerFactory.getLogger(AlphaNumericControlFieldImpl.class);
	
    private static final long serialVersionUID = 8049827626175226331L;

    private String myData;

    private static final Set<String> RESERVED_CONTROL_FIELDS = ImmutableSet.of("DNK", "DOK", 
    		"FMT", "LDR", "NSS", "SIG", "SYS", "UST", "VVS", "VVV", "ZPT", "0FS", "---", 
    		"0 8", "020", "022", "024", "035", "048", "082", "245", "246", "250", "300", 
    		"310", "362", "490", "500", "504", "505", "515", "521", "546", "610", "648", 
    		"653", "700", "710", "780", "856", "902", "962", "967", "984", "990", "991", 
    		"992", "993", "994", "^pD", "^pG", "^pL", "^pN", "^pP", "^pS", "^pZ");

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
    		logger.info(aTag + " is not a valid ControlField tag");
//            throw new InvalidMARCException(aTag +
//                    " is not a valid ControlField tag");
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
