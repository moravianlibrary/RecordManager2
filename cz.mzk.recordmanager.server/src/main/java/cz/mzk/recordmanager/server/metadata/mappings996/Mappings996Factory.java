package cz.mzk.recordmanager.server.metadata.mappings996;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Mappings996Enum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Mappings996Factory {

	@Autowired
	private ApplicationContext appCtx;

	public Mappings996 getMappings996(HarvestedRecord hr) {
		Mappings996Enum type = hr.getHarvestedFrom().getMappings996();
		if (type == null) type = Mappings996Enum.DEFAULT;
		switch (type) {
			case ALEPH:
				return new AlephMappings996();
			case CASLIN:
				return init(new CaslinMappings996());
			case DAWINCI:
				return new DawinciMappings996();
			case KOHA:
				return new KohaMappings996();
			case TRITIUS:
				return new TritiusMappings996();
			default:
				return new DefaultMappings996();
		}
	}

	private Mappings996 init(Mappings996 mappings996) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(mappings996);
		factory.initializeBean(mappings996, "mappings996");
		return mappings996;
	}

}
