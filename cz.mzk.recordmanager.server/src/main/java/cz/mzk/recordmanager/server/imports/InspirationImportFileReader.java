package cz.mzk.recordmanager.server.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.ItemReader;

public class InspirationImportFileReader implements ItemReader<Map<String, List<String>>> {

	private BufferedReader br;
	
	private static final Pattern PATTERN_NAME = Pattern.compile("^\\[([^\\]]*)\\]$");
	private static final Pattern PATTERN_ID = Pattern.compile("^[^\\.]*\\..*");

	public InspirationImportFileReader(String filename) throws FileNotFoundException {
		br = new BufferedReader(new FileReader(new File(filename)));
	}
	
	@Override
	public Map<String, List<String>> read() throws IOException {
		if(br.ready()) return next();
		return null;
	}
	
	private Map<String, List<String>> next() throws IOException{		
		Map<String, List<String>>  result = new HashMap<>();
		
		String name = null;
		Set<String> ids = new HashSet<String>();
		Matcher matcher;
		String newLine = null;
		
		while(br.ready()){
			newLine = br.readLine();
			
			if(newLine.isEmpty() && name != null){
				break;
			}
			matcher = PATTERN_NAME.matcher(newLine);
			if(matcher.matches()){
				name = matcher.group(1);
			}		
			matcher = PATTERN_ID.matcher(newLine);
			if(matcher.matches()){
				ids.add(newLine);
			}
		}
		
		result.put(name, new ArrayList<String>(ids));
		
		return result;
	}
	

}
