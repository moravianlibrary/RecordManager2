package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ViewTypeTest extends AbstractTest {

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Test
	public void getAllPossibleValuesTest() throws Exception {
		List<String> data = new ArrayList<>();
		data.add("100 $aKarel");
		MarcRecordImpl mri = MarcRecordFactory.recordFactory(data);
		MetadataRecord metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(ViewType.getPossibleValues(metadataRecord).containsAll(
				Arrays.stream(ViewType.values()).map(ViewType::getValue).collect(Collectors.toList())));
	}
}
