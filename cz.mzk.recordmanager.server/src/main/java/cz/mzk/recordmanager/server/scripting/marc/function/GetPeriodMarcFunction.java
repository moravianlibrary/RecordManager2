package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
@Component
public class GetPeriodMarcFunction implements MarcRecordFunctions{


    /**
     * get the era field values from 045a as a Set of Strings
     */
    public Set<String> getPeriod(MarcRecord record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String period = record.getField("045", 'a');
        if(period == null)
            return result;
        if(period.length() == 4)
        {
            period = period.toLowerCase();
            char periodStart1 = period.charAt(0);
            char periodStart2 = period.charAt(1);
            char periodEnd1 = period.charAt(2);
            char periodEnd2 = period.charAt(3);
            
            return getPeriod(result, periodStart1, periodStart2, periodEnd1, periodEnd2);
        }
        else if (period.length() == 5)
        {
            char periodStart1 = period.charAt(0);
            char periodStart2 = period.charAt(1);

            char periodEnd1 = period.charAt(3);
            char periodEnd2 = period.charAt(4);
            char gap = period.charAt(2);
            if(gap == ' ' || gap == '-')
            	return getPeriod(result, periodStart1, periodStart2, periodEnd1, periodEnd2);
        }
        else if (period.length() == 2)
        {
            char periodStart1 = period.charAt(0);
            char periodStart2 = period.charAt(1);
            if(periodStart1 >= 'a' && periodStart1 <= 'y' && 
                    periodStart2 >= '0' && periodStart2 <= '9')
            	return getPeriod(result, periodStart1, periodStart2, periodStart1, periodStart2);
        }
        return result;
    }

    /**
     * get the two eras indicated by the four passed characters, and add them
     *  to the result parameter (which is a set).  The characters passed in are
     *  from the 045a.
     */
    private Set<String> getPeriod(Set<String> result, char periodStart1, char periodStart2, char periodEnd1, char periodEnd2)
    {
    	if(periodStart1 == 'a' && periodStart2 == '0') {
    		result.add(getPeriod('a', 0));
    		periodStart1 = 'b';
    	}
    	if(periodStart1 >= 'a' && periodStart1 <= 'y' && periodEnd1 >= 'a' && periodEnd1 <= 'y')
        {
            for(char periodVal = periodStart1; periodVal <= periodEnd1; periodVal++)
            {
                if(periodStart2 != '-' || periodEnd2 != '-')
                {
                    char loopStart = (periodVal != periodStart1) ? '0' : Character.isDigit(periodStart2) ? periodStart2 : '0';
                    char loopEnd = (periodVal != periodEnd1) ? '9' : Character.isDigit(periodEnd2) ? periodEnd2 : '9';
                    for(char periodVal2 = loopStart; periodVal2 <= loopEnd; periodVal2++)
                    {
                        result.add("" + getPeriod(periodVal,Character.getNumericValue(periodVal2)));
                    }
                }
                else result.add("" + getPeriod(periodVal, 0));
            }
        }
        return result;
    }

    private String getPeriod(char c, int i){
    	String period = "";
    	
    	if(c == 'a'){
    		return "? - 2999";
    	}
    	if(c > 'a' && c <= 'd'){
    		int x = 2999 - (10 * (c - 'a' - 1) + i) * 100;
   
    		return Integer.toString(x) + " - " + Integer.toString(x-99) + " Před n. l.";
    	}
    	else if(c >= 'e' && c <= 'y'){
    		i = 0;
    		int x = (10 * (c - 'e') + i) * 10;
    		
    		return Integer.toString(x) + " - " + Integer.toString(x+99);
    	}
    	
    	return period;
    }
}
