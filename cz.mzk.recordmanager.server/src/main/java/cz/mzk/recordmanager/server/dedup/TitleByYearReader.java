package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dedup.clustering.NonperiodicalTitleClusterable;
import cz.mzk.recordmanager.server.oai.dao.TitleDAO;

@Component
public class TitleByYearReader implements ItemReader<List<NonperiodicalTitleClusterable>> {

	
	@Autowired
	private TitleDAO titleDao;
	
	private static final Long MIN_YEAR = 1900L;
	
	private static final Long MAX_YEAR = 2015L;
	
	private String[] langCodes = {"cze","eng", "oth"};
	
	private Long currentYear = MIN_YEAR;
	
	private int paggingIndex = 0;
	
	List<Triple<Integer, Integer, String>> paggingIntervals = new ArrayList<>();
	
	public TitleByYearReader() {
		for (String code: langCodes) {
			paggingIntervals.add(Triple.of(new Integer(0), new Integer(50), code));
			paggingIntervals.add(Triple.of(new Integer(50), new Integer(100), code));
			paggingIntervals.add(Triple.of(new Integer(100), new Integer(150), code));
			paggingIntervals.add(Triple.of(new Integer(150), new Integer(500), code));
			paggingIntervals.add(Triple.of(new Integer(500), new Integer(10000), code));
		}
	}
	
	@Override
	public List<NonperiodicalTitleClusterable> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		
		if (currentYear > MAX_YEAR) {
			return null;
		}
		
		if (paggingIndex >= paggingIntervals.size()) {
			paggingIndex = 0;
			currentYear++;
		}
		
		Triple<Integer, Integer, String> interval = paggingIntervals.get(paggingIndex++);
		List<NonperiodicalTitleClusterable> result = titleDao.getTitleForDeduplicationByYear(currentYear, interval.getLeft(), interval.getMiddle(), interval.getRight());
		
		return result;
	}

}
