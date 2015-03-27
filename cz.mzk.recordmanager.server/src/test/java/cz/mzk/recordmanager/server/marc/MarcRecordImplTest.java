package cz.mzk.recordmanager.server.marc;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
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
}
