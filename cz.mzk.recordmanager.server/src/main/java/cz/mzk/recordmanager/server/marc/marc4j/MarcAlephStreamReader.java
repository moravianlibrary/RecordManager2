package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.marc4j.Constants;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class MarcAlephStreamReader implements MarcReader{

	private BufferedReader br;

    private MarcFactory factory;
    
    private static final Pattern LDR_PATTERN = Pattern.compile("^[^ ]* LDR.*$");
    
    private static final String LDR_TAG = "LDR";
    
    /**
     * Constructs an instance with the specified input stream.
     */
    public MarcAlephStreamReader(InputStream input) {
    	this(input, null);
    }

	/**
     * Constructs an instance with the specified input stream.
     */
    public MarcAlephStreamReader(InputStream input, String encoding) {
    	br = new BufferedReader(new InputStreamReader(
                new DataInputStream((input.markSupported()) ? input
                        : new BufferedInputStream(input))));
        factory = MarcFactoryImpl.newInstance();
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    public boolean hasNext() {
    	try {
			return br.ready();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    public Record next() {
        return nextRecord();
    }
    
    private Record nextRecord() {
    	try {
    		Record rec = null;
    		String newLine;
			while((newLine = br.readLine()) != null){
				if(newLine == "" || newLine.isEmpty()) return rec;
	    		if(LDR_PATTERN.matcher(newLine).find()) rec = factory.newRecord();
	    		parseLine(rec, newLine);
	    	}
			return rec;
		} catch (IOException e) {
			e.printStackTrace();
		}

    	return null;
	}

    private void parseLine(Record record, String newLine) {
    	
    	String arrayRec[] = newLine.split("\n");
    	int idLength = arrayRec[0].indexOf(" ");
    	int indexData = idLength+9;
		String f001 = arrayRec[0].substring(0, idLength);
		boolean exists001 = false;
		
    	for(String line : arrayRec) {
			String tag = line.substring(idLength+1, idLength+4);
			char ind1 = line.charAt(idLength+4);
			char ind2 = line.charAt(idLength+5);
			String data = line.substring(indexData);
			
			if(tag.matches(LDR_TAG)){
				Leader ldr;
		        ldr = factory.newLeader(data);
		        record.setLeader(ldr);
			}
			else if(Constants.CF_TAG_PATTERN.matcher(tag).find()){
				record.addVariableField(factory.newControlField(tag, data));
				if(tag.matches("001")) exists001 = true;
			}
			else{
				DataField df = factory.newDataField(tag, ind1, ind2);
				df = parseDataField(df, data);
				record.addVariableField(df);
			}
		}
    	if(!exists001) record.addVariableField(factory.newControlField("001", f001));
    }

    private DataField parseDataField(DataField df, String data){
    	String a[] = data.split("\\$\\$");
    	for(String sbstr : Arrays.copyOfRange(a, 1, a.length)){
    		df.addSubfield(factory.newSubfield(sbstr.charAt(0), sbstr.substring(1)));
    	}
    	
    	return df;
    }
}
