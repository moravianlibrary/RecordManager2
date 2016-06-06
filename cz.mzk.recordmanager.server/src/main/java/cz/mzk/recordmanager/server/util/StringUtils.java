package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.model.Title;

public class StringUtils {
	
	public static final int MAX_MATCH_BOUNDARY = 100;
	
	public static final int MIN_MATCH_BOUNDARY = 0;
	
	public static boolean simmilarTitleMatch(Title titleA, Title titleB, int matchBoundary, int prefixBoundary) {
		return simmilarTitleMatchPercentage(titleA, titleB, matchBoundary, prefixBoundary) >= matchBoundary;
	}
	
	public static boolean simmilarTitleMatch(String strA, String strB, int matchBoundary, int prefixBoundary) {
		return simmilarTitleMatchPercentage(strA, strB, matchBoundary, prefixBoundary) >= matchBoundary;
	}
	
	public static int simmilarTitleMatchPercentage(Title titleA, Title titleB, int matchBoundary, int prefixBoundary) {
		
		if (titleA == null || titleA.getTitleStr() == null) {
			return MIN_MATCH_BOUNDARY;
		}
		
		if (titleB == null || titleB.getTitleStr() == null) {
			return MIN_MATCH_BOUNDARY;
		}
		
		
		if(titleA.isSimilarityEnabled() && titleB.isSimilarityEnabled()){
			return simmilarTitleMatchPercentage(titleA.getTitleStr(), titleB.getTitleStr(), matchBoundary, prefixBoundary);
		}

		return titleA.getTitleStr().equals(titleB.getTitleStr()) ? MAX_MATCH_BOUNDARY : MIN_MATCH_BOUNDARY;
	}
	
	public static int simmilarTitleMatchPercentage(String strA, String strB, int matchBoundary, int prefixBoundary) {
		
		if (strA == null || strB == null
				|| (strA.isEmpty() && strB.isEmpty())) {
			return MIN_MATCH_BOUNDARY;
		}
		
		int minLength = Math.min(strA.length(), strB.length());
		
		if (minLength > prefixBoundary) {
			if (strA.startsWith(strB) || strB.startsWith(strA)) {
				return MAX_MATCH_BOUNDARY;
			}
		}

		int maxDiff = (int) Math.ceil(minLength * (1 - (double) (matchBoundary / 100)));
		
		int distance = org.apache.commons.lang3.StringUtils.getLevenshteinDistance(strA, strB, maxDiff);
		if (distance < 0) {
			return MIN_MATCH_BOUNDARY;
		}
		
		float percentage = ((float) distance) / minLength;
		return (int) ((1 - percentage) * 100);
	}
}
