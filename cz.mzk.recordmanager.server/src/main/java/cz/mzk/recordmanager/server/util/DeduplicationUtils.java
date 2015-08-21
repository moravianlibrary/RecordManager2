package cz.mzk.recordmanager.server.util;

public class DeduplicationUtils {

	/**
	 * 
	 * @param pages1
	 * @param pages2
	 * @param boundary maximal percentual difference
	 * @param absoluteDiference maximal absolute difference
	 * @return true if false if pages, true if at least one is null or they are in safe interval
	 */
	public static boolean comparePages(Long pages1, Long pages2, double boundary, int absoluteDiference) {
		if (pages1 == null || pages2 == null) {
			return true;
		}
		
		Long min = Math.min(pages1, pages2);
		Long max = Math.max(pages1, pages2);
		
		double safeIntervalLegth = Math.ceil(min.doubleValue() * boundary);
		if (safeIntervalLegth > absoluteDiference / 2) {
			safeIntervalLegth = absoluteDiference / 2;
		}

		return (max >= (min - safeIntervalLegth) && max <= (min + safeIntervalLegth));
	}
}
