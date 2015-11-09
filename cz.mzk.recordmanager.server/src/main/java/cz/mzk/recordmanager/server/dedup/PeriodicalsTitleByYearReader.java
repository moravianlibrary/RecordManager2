package cz.mzk.recordmanager.server.dedup;

import java.util.List;
import java.util.Stack;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dedup.clustering.TitleClusterable;
import cz.mzk.recordmanager.server.oai.dao.TitleDAO;

public class PeriodicalsTitleByYearReader implements
		ItemReader<List<TitleClusterable>> {

	@Autowired
	private TitleDAO titleDao;
	
	private Stack<Long> years = new Stack<>();
	
	public PeriodicalsTitleByYearReader(long minYear, long maxYear) {
		for (long l = maxYear; l >= minYear; l--) {
			years.push(l);
		}
	}




	@Override
	public List<TitleClusterable> read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
		
		if (years.isEmpty()) {
			return null;
		}
		
		return titleDao.getPeriodicalsTitleForDeduplication(years.pop());
	}

}
