package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.function.MarcRecordFunction;
import cz.mzk.recordmanager.server.scripting.function.MarcRecordFunctions;

@Component
public class MarcScriptFactoryImpl extends AbstractScriptFactory<MarcRecord>
		implements MarcScriptFactory, InitializingBean {

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired
	private List<MarcRecordFunctions> functionsList;

	private Map<String, MarcRecordFunction> functions;

	@Override
	public MarcMappingScript create(InputStream... scriptsSource) {
		return (MarcMappingScript) super.create(scriptsSource);
	}

	@Override
	protected MarcMappingScript create(Binding binding,
			List<DelegatingScript> scripts) {
		return new MarcMappingScriptImpl(binding, scripts, propertyResolver,
				functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		functions = new HashMap<>();
		for (final MarcRecordFunctions funcs : functionsList) {
			for (Method method : funcs.getClass().getMethods()) {
				if (Modifier.isPublic(method.getModifiers())) {
					MarcRecordFunction func = new MarcRecordFunction() {

						@Override
						public Object apply(MarcRecord record, Object... args) {
							try {
								if (method.getParameterCount() == 1) {
									return method.invoke(funcs, record);
								} else {
									return method.invoke(funcs, new Object[]{record, args});
								}
							} catch (IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}

					};
					functions.put(method.getName(), func);
				}
			}
		}
	}

}
