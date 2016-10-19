package cz.mzk.recordmanager.server.marc.marc4j;

import info.freelibrary.marc4j.impl.SubfieldImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.InvalidMARCException;
import org.marc4j.marc.Subfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * customized {@link DataField} implementation. Allows usage of alphanumeric MARC fields
 *
 */
public class DataFieldImpl extends info.freelibrary.marc4j.impl.VariableFieldImpl implements DataField {

	private static Logger logger = LoggerFactory.getLogger(DataFieldImpl.class);
	
	private static final long serialVersionUID = 1L;

	private char myFirstInd;

    private char mySecondInd;

    private List<Subfield> mySubfields = new ArrayList<Subfield>();

    private static final Set<String> RESERVED_DATA_FIELDS = ImmutableSet.of("002", "003", "007");
    
    DataFieldImpl() {
    	
    }
    
    /**
     * Creates a new <code>DataField</code> and sets the tag name and the first
     * and second indicator.
     * 
     * @param aTag The tag name
     * @param aFirstInd The first indicator
     * @param aSecondInd The second indicator
     */
    DataFieldImpl(String aTag, char aFirstInd, char aSecondInd) {
        setTag(aTag);
        setIndicator1(aFirstInd);
        setIndicator2(aSecondInd);
    }

    /**
     * Sets the tag of a <code>DataField</code>.
     * 
     * @param aTag The tag of a <code>DataField</code>
     */
    public void setTag(String aTag) {
    	super.setTag(aTag);

        if (aTag.length() == 3) {

                if (aTag.startsWith("00") && !RESERVED_DATA_FIELDS.contains(aTag)) {
                	logger.info(aTag + " is not a valid DataField tag");
//                    throw new InvalidMARCException(aTag +
//                            " is not a valid DataField tag");
                }

        } else {
            throw new InvalidMARCException(aTag + " is not a three digit tag");
        }
    }

    /**
     * Sets the field's first indicator.
     * 
     * @param aFirstInd The first indicator
     */
    public void setIndicator1(char aFirstInd) {
        myFirstInd = aFirstInd;
    }

    /**
     * Returns the field's first indicator.
     * 
     * @return The field's first indicator
     */
    public char getIndicator1() {
        return myFirstInd;
    }

    /**
     * Sets the field's second indicator.
     * 
     * @param aSecondInd The field's second indicator
     */
    public void setIndicator2(char aSecondInd) {
        mySecondInd = aSecondInd;
    }

    /**
     * Returns the field's second indicator
     * 
     * @return The field's second indicator
     */
    public char getIndicator2() {
        return mySecondInd;
    }

    /**
     * Adds a <code>Subfield</code>.
     * 
     * @param aSubfield The <code>Subfield</code> of a <code>DataField</code>
     * @throws IllegalAddException when the parameter is not an instance of
     *         <code>SubfieldImpl</code>
     */
    public void addSubfield(Subfield aSubfield) {
        if (aSubfield instanceof SubfieldImpl) {
            mySubfields.add(aSubfield);
        } else {
            throw new IllegalAddException(
                    "Supplied Subfield isn't an instance of SubfieldImpl");
        }
    }

    /**
     * Inserts a <code>Subfield</code> at the specified position.
     * 
     * @param aIndex The subfield's position within the list
     * @param aSubfield The <code>Subfield</code> object
     * @throws IllegalAddException when supplied Subfield isn't an instance of
     *         <code>SubfieldImpl</code>
     */
    public void addSubfield(int aIndex, Subfield aSubfield) {
        mySubfields.add(aIndex, aSubfield);
    }

    /**
     * Removes a <code>Subfield</code> from the field.
     * 
     * @param aSubfield The subfield to remove from the field.
     */
    public void removeSubfield(Subfield aSubfield) {
        mySubfields.remove(aSubfield);
    }

    /**
     * Returns the list of <code>Subfield</code> objects.
     * 
     * @return The list of <code>Subfield</code> objects
     */
    public List<Subfield> getSubfields() {
        // TODO: consistent result/expectation as getSubfields(char)?
        return mySubfields;
    }

    /**
     * Returns the {@link Subfield}s with the supplied <code>char</code> code.
     * 
     * @param aCode A subfield code
     * @return A {@link List} of {@link Subfield}s
     */
    public List<Subfield> getSubfields(char aCode) {
        List<Subfield> subfields = new ArrayList<Subfield>();

        for (Subfield subfield : mySubfields) {
            if (subfield.getCode() == aCode) {
                subfields.add(subfield);
            }
        }

        return subfields;
    }

    /**
     * Returns the number of subfields in this <code>DataField</code>.
     * 
     * @return The number of subfields in this <code>DataField</code>
     */
    public int countSubfields() {
        return mySubfields != null ? mySubfields.size() : 0;
    }

    /**
     * Returns the first {@link Subfield} matching the supplied
     * <code>char</code> code.
     * 
     * @param aCode A code for the subfield to be returned
     */
    public Subfield getSubfield(char aCode) {
        for (Subfield subfield : mySubfields) {
            if (subfield.getCode() == aCode) {
                return subfield;
            }
        }

        return null;
    }

    /**
     * Returns <code>true</code> if a match is found for the supplied regular
     * expression pattern; else, <code>false</code>.
     * 
     * @param aPattern A regular expression pattern to find in the subfields
     */
    public boolean find(String aPattern) {
        for (Subfield subfield : mySubfields) {
            if (subfield.find(aPattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a string representation of this data field.
     * <p>
     * Example:
     * 
     * <pre>
     *    245 10$aSummerland /$cMichael Chabon.
     * </pre>
     * 
     * @return A string representation of this data field
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(' ');
        sb.append(getIndicator1());
        sb.append(getIndicator2());

        for (Subfield subfield : mySubfields) {
            sb.append(subfield.toString());
        }

        return sb.toString();
    }
}
