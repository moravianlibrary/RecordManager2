package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class MarcScriptFactoryImpl extends AbstractScriptFactory<MarcRecord> implements MarcScriptFactory {

	@Override
	public MarcMappingScript create(InputStream... scriptsSource) {
		return (MarcMappingScript) super.create(scriptsSource);
	}

	@Override
	protected MarcMappingScript create(Binding binding, List<DelegatingScript> scripts) {
		return new MarcMappingScriptImpl(binding, scripts);
	}

}
