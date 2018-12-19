package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewTypeTest extends AbstractTest {

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Autowired
	private ListResolver resolver;

	@Test
	public void getAllPossibleValuesTest() throws Exception {
		List<String> data = new ArrayList<>();
		data.add("100 $aKarel");
		data.add("072   $a00$91");
		MarcRecordImpl mri = MarcRecordFactory.recordFactory(data);
		MetadataRecord metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(ViewType.getPossibleValues(metadataRecord, resolver, 300L).containsAll(
				Arrays.asList(ViewType.IREL.getValue(), ViewType.KIV.getValue())));
	}
}
