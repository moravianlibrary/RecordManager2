package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecordFactory;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;

@Test
public class MzkStatusFunctionsTest extends AbstractTest {

	@Autowired
	private MzkStatusFunctions statusFunctions;

	public void presentStatus() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("996 $b2610651530 $c4-1355.961 $lMZK $rSklad / do 1 hodiny $n0$p p.v. $w001533248 $u000010$a1 $eBOA001 $jMZK50 $sP");
		Set<String> statuses = statusFunctions.getMZKStatuses(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(statuses, ImmutableSet.of("present"));
	}

}
