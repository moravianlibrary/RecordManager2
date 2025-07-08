package cz.mzk.recordmanager.server.scripting.marc.function.nkp;

import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;
import cz.mzk.recordmanager.server.scripting.marc.function.mzk.MzkStatusFunctions;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class NkpStatusFunctions extends MzkStatusFunctions implements MarcRecordFunctions {


	public Set<String> getNkpStatuses(MarcFunctionContext ctx) {
		Set<String> results = super.getMZKStatuses(ctx, true);
		EnumSet<AvailabilityStatus> statuses = EnumSet.noneOf(AvailabilityStatus.class);
		ctx.record().getDataFields(STATUS_FIELD).forEach(field -> {
			for (AvailabilityStatus status : AvailabilityStatus.values()) {
				if (status.check(field)) {
					statuses.add(status);
				}
			}
		});
		if (statuses.contains(AvailabilityStatus.LIMITED)) {
			results.add(LIMITED_STATUS);
		}
		if (isNkpEod(ctx)) {
			results.add(EOD_STATUS);
		}
		return results;
	}


	protected boolean isNkpEod(MarcFunctionContext ctx) {
		return ctx.metadataRecord().isEod();
	}
}
