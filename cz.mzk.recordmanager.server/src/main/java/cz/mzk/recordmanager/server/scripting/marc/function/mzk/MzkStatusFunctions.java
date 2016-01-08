package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MzkStatusFunctions implements MarcRecordFunctions {

	private static final String STATUS_FIELD = "996";

	private static final String ABSENT_STATUS = "absent";

	private static final String PRESENT_STATUS = "present";

	private static final String FREE_STACK_STATUS = "free_stack";

	private static final String EOD_STATUS = "available_for_eod";

	private static final String ONLINE_STATUS = "available_online";

	private static final String EOD_FIELD = "993";

	private static final List<String> ONLINE_FIELDS = Arrays.asList( //
			"996", //
			"856" //
	);

	private static final List<String> ONLINE_URL_PREFIXES = Arrays.asList( //
			"http://kramerius.mzk.cz/", //
			"http://imageserver.mzk.cz/" //
	);

	private static enum AvailabilityStatus {
		ABSENT {
			@Override
			public boolean check(DataField df) {
				return check(df, 's', "a");
			}
		},
		PRESENT {
			@Override
			public boolean check(DataField df) {
				return check(df, 's', "p");
			}
		},
		FREE_STACK {
			@Override
			public boolean check(DataField df) {
				return check(df, 'a', "0");
			}
		};

		public abstract boolean check(DataField df);

		public boolean check(DataField dataField, char subfieldCode, String value) {
			Subfield subfield = dataField.getSubfield(subfieldCode);
			if (subfield == null || subfield.getData() == null) {
				return false;
			}
			return subfield.getData().toLowerCase().trim().equals(value);
		}

	}

	public Set<String> getMzkStatuses(MarcFunctionContext ctx) {
		EnumSet<AvailabilityStatus> statuses = EnumSet.noneOf(AvailabilityStatus.class);
		ctx.record().getDataFields(STATUS_FIELD).forEach(field -> {
			for (AvailabilityStatus status : AvailabilityStatus.values()) {
				if (status.check(field)) {
					statuses.add(status);
				}
			}
		});
		Set<String> result = new HashSet<String>();
		if (statuses.contains(AvailabilityStatus.ABSENT)) {
			result.add(ABSENT_STATUS);
		}
		if (statuses.contains(AvailabilityStatus.PRESENT)) {
			result.add(PRESENT_STATUS);
		}
		if (statuses.contains(AvailabilityStatus.FREE_STACK) && result.isEmpty()) {
			result.add(FREE_STACK_STATUS);
		}
		if (isEod(ctx)) {
			result.add(EOD_STATUS);
		}
		if (isOnline(ctx)) {
			result.add(ONLINE_STATUS);
		}
		return result;
	}

	protected boolean isEod(MarcFunctionContext ctx) {
		return ctx.record().getFields(EOD_FIELD, 'a').contains("Y");
	}

	protected boolean isOnline(MarcFunctionContext ctx) {
		for (String field : ONLINE_FIELDS) {
			for (String url : ctx.record().getFields(field, 'u')) {
				for (String onlinePrefix : ONLINE_URL_PREFIXES) {
					if (url.startsWith(onlinePrefix)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
