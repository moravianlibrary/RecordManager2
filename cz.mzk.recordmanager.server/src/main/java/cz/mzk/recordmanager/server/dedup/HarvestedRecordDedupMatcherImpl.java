package cz.mzk.recordmanager.server.dedup;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvester;

@Component
public class HarvestedRecordDedupMatcherImpl implements
		HarvestedRecordDedupMatcher {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);
	
	private static final int TITLE_MATCH_TRESHOLD = 50;

	
	
	@Override
	public  boolean matchRecords(final HarvestedRecord record1, final HarvestedRecord record2) {
		//TODO unused???
//		boolean isbnMatch = false;
//		boolean titleMatch = false;
//		
//		if (record1.getPublicationYear() != null 
//				&& record2.getPublicationYear() != null 
//				&& !record1.getPublicationYear().equals(record2.getPublicationYear())) {
//			logger.debug("Publication year mismatch, original: {} deduplicated: {}", record1, record2);
//			return false;
//		}
//				
//		if (record1.getPhysicalFormat() != null 
//				&& record2.getPhysicalFormat() != null 
//				&& !record1.getPhysicalFormat().equals(record2.getPhysicalFormat())) {
//			logger.debug("Format mismatch, original: {} deduplicated: {}", record1, record2);
//			return false;
//		}
//		
//		if (record1.getIsbn() != null && record2.getIsbn() != null) {
//			isbnMatch = record1.getIsbn().equals(record2.getIsbn());
//		}
//		
//		if (record1.getTitle() != null && record2.getTitle() != null) {
//			if (levensteinTitleMatch(record1.getTitle(), record2.getTitle()) > TITLE_MATCH_TRESHOLD) {
//				titleMatch = true;
//			}
//		}
//		
//		if (titleMatch || isbnMatch) {
//			return true;
//		}
		
		return deepMatchRecords(record1, record2);
	}
	
	protected boolean deepMatchRecords(final HarvestedRecord origrRecord, final HarvestedRecord dedupRecord) {
		//TODO implementation
		return false;
	}
	
	/**
	 * @param origTitle
	 * @param dedupTitle
	 * @return percentage of match based of Levenstein distance (0 - 100)
	 */
	protected int levensteinTitleMatch(final String origTitle,
			final String dedupTitle) {
		if (origTitle == null || dedupTitle == null
				|| (origTitle.isEmpty() && dedupTitle.isEmpty())) {
			return 0;
		}
		int dist = StringUtils.getLevenshteinDistance(origTitle, dedupTitle);
		float percentage = ((float) dist) / Math.min(origTitle.length(), dedupTitle.length());
		return (int) ((1 - percentage) * 100);

	}

}
