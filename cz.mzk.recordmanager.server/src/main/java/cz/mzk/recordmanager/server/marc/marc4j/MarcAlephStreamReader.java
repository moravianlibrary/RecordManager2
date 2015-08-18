package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.regex.Pattern;

import org.marc4j.Constants;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class MarcAlephStreamReader implements MarcReader{

    private DataInputStream input = null;

    private Record record;

    private MarcFactory factory;

    private Deque<String> queue = null;
    
    private static final Pattern LDR_PATTERN = Pattern.compile("^[^ ]* LDR.*$");
    
    private static final String LDR_TAG = "LDR";
    
    /**
     * Constructs an instance with the specified input stream.
     */
    public MarcAlephStreamReader(InputStream input) {
    	this(input, null);
    	splitRecords(this.input);
    }

	/**
     * Constructs an instance with the specified input stream.
     */
    public MarcAlephStreamReader(InputStream input, String encoding) {
        this.input =
                new DataInputStream((input.markSupported()) ? input
                        : new BufferedInputStream(input));
        factory = MarcFactoryImpl.newInstance();
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    public boolean hasNext() {
    	return !queue.isEmpty();
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    public Record next() {
        record = factory.newRecord();
        String stringRecord = queue.pop();
        parseRecord(record, stringRecord);
        return record;
    }
    
    private void splitRecords(InputStream input) {
    	BufferedReader br = new BufferedReader(new InputStreamReader(input));
    	queue = new ArrayDeque<String>();
    	try {
    		StringBuilder rec = new StringBuilder();
    		String newLine;
			while((newLine = br.readLine()) != null){
				if(newLine == "") continue;
	    		if(LDR_PATTERN.matcher(newLine).find()){
	    			if(rec.length() != 0){
	    				queue.push(rec.toString());
	    			}
	    			rec = new StringBuilder();
	    			rec.append(newLine);
	    			rec.append("\n");
	    		}
	    		else{
	    			rec.append(newLine);
	    			rec.append("\n");
	    		}

	    	}
			queue.push(rec.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}   	
		
	}

    private void parseRecord(Record record, String strRecord) {
    	
    	String arrayRec[] = strRecord.split("\n");
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
