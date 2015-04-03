package cz.mzk.recordmanager.server.marc;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class MarcRecordImplTest extends AbstractTest {
    
	@Autowired
	private HarvestedRecordDAO hrdao;
	
	@Autowired
	private MarcXmlParser parser;
	
	@BeforeMethod
	public void init() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}   
	
	@Test
    public void getPublicationYearTest() throws Exception{
    	MarcRecordImpl mri;
    	
    	List<String> data = new ArrayList<String>();
    	data.add("260 $c1977");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $c[1977]");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $cc1977");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $cc[1977]");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $cp1977");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $cp[1977]");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $c1977-2003");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);
    	data.clear();
    	
    	data.add("260 $c1977 printing, c1975");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $c1977-");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $c1977, c1978");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $cApril 15, 1977");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $c1977[i.e. 1971]");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();  
    	
    	data.add("260 $c<1977->");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getPublicationYear().longValue(), 1977);    	
    	data.clear();
    	
    	data.add("260 $c197-");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertNull(mri.getPublicationYear());    	
    	data.clear();
    	
    	data.add("260 $casdba");    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertNull(mri.getPublicationYear());    	
    	data.clear();    	
    }	

	@Test
    public void getISSNsTest() throws Exception{
        MarcRecordImpl mri;    	
    	List<String> data = new ArrayList<String>();
    	
    	data.add("022 $a2336-4815");
    	data.add("022 $a1234-5678");
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getISSNs().toString(), "[23364815, 12345678]");
    	data.clear();
    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getISSNs(), Collections.emptyList());
    	data.clear();
    }
	
	@Test 
	void getSeriesISSNTest() throws Exception{
		MarcRecordImpl mri;
		List<String> data = new ArrayList<String>();
  	  
		data.add("490 $x0023-6721");
  	  	mri = MarcRecordFactory.recordFactory(data);
  	  	Assert.assertEquals(mri.getSeriesISSN(), "0023-6721");
  	  	data.clear();
  	  	
  	    mri = MarcRecordFactory.recordFactory(data);
	  	Assert.assertEquals(mri.getSeriesISSN(), null);
	  	data.clear();
	}
	
	@Test 
	void getPageCountTest() throws Exception{
	    MarcRecordImpl mri;
	    List<String> data = new ArrayList<String>();
	  	 
	    data.add("300 $a257 s.");
	  	mri = MarcRecordFactory.recordFactory(data);
	  	Assert.assertEquals(mri.getPageCount().longValue(), 257);
	  	data.clear();
	  	
	  	data.add("300 $a1 zvuková deska (78:24)");
	  	mri = MarcRecordFactory.recordFactory(data);
	  	Assert.assertEquals(mri.getPageCount().longValue(), 1);
	  	data.clear();
	  	
	  	data.add("300 $a[14] s.");
	  	mri = MarcRecordFactory.recordFactory(data);
	  	Assert.assertEquals(mri.getPageCount().longValue(), 14);
	  	data.clear();

	  	mri = MarcRecordFactory.recordFactory(data);
	  	Assert.assertEquals(mri.getPageCount(), null);
	  	data.clear();
	}
	
	@Test
    public void getISBNsTest() throws Exception{
        MarcRecordImpl mri;    	
    	List<String> data = new ArrayList<String>();
    	
    	data.add("020 $a9788086026923");
    	data.add("020 $a9788086026923");
    	data.add("020 $a978-80-7250-482-4");
    	data.add("020 $a80-200-0980-9");
    	data.add("020 $a456");
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getISBNs().toString(), "[9788086026923, 9788072504824, 9788020009807]");
    	data.clear();
    	
    	mri = MarcRecordFactory.recordFactory(data);
    	Assert.assertEquals(mri.getISBNs(), Collections.emptyList());
    	data.clear();
    }
	
}
