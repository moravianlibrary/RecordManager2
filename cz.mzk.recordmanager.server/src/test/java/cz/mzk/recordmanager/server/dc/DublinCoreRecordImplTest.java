package cz.mzk.recordmanager.server.dc;

import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

public class DublinCoreRecordImplTest extends AbstractTest {

	
  @Test
  public void getDateTest() {
	     
	  DublinCoreRecord rec = new DublinCoreRecordImpl();
	  String date = rec.getFirstDate();
	  Assert.assertEquals(null,date);
	  Assert.assertTrue(rec.getDates().isEmpty());
	  
	  String testDate = "1984";
      String testDate2 = "2015";
	  
/*	  System.out.println("Když nenastavíme title, máme: "+title);*/
	  rec.addDate(testDate);	 
	  rec.addDate(testDate2);
	  
	  date = rec.getFirstDate();
	  Assert.assertEquals(testDate,date);
	  
	  String dateFromList = rec.getDates().get(0);
	  Assert.assertEquals(testDate,dateFromList);
	  	  
	  dateFromList = rec.getDates().get(1);
	  Assert.assertEquals(testDate2,dateFromList);
	  
/*	  System.out.println("Když nastavíme title, máme:" +title);*/
  }	
	
  @Test
  public void getTitleTest() {
	  DublinCoreRecord rec = new DublinCoreRecordImpl();

	  String title = rec.getFirstTitle();
	  Assert.assertEquals(null,title);
	  Assert.assertTrue(rec.getTitles().isEmpty());
	  
/*	  System.out.println("Když nenastavíme title, máme: "+title);*/
	  
	  String testTitle = "Babička";
	  String testTitle2 = "Dědeček hříbeček";
	  
	  rec.addTitle(testTitle);	 
	  rec.addTitle(testTitle2);
	  
	  title = rec.getFirstTitle();
	  Assert.assertEquals(testTitle,title);
	  
	  String titleFromList = rec.getTitles().get(0);
	  Assert.assertEquals(testTitle,titleFromList);
	  
	  titleFromList = rec.getTitles().get(1);
	  Assert.assertEquals(testTitle2,titleFromList);
	  
/*	  System.out.println("Když nastavíme title, máme:" +title);*/
  }
	
  
}
