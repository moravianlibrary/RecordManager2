package cz.mzk.recordmanager.server.scripting.marc;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.AbstractScriptFactory;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunction;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MarcScriptFactoryImpl extends AbstractScriptFactory<MarcRecord>
		implements MarcScriptFactory, InitializingBean {

	private static class MarcRecordFunctionImpl implements MarcRecordFunction {

		private final MarcRecordFunctions target;

		private final Method method;

		public MarcRecordFunctionImpl(MarcRecordFunctions target, Method method) {
			super();
			this.target = target;
			this.method = method;
		}

		@Override
		public Object apply(MarcRecord record, Object args) {
			Object arg = record;
			try {
				if (args instanceof Object[] && ((Object[]) args).length >= 1) {
					Object[] argsAsArray = (Object[]) args; 
					Object[] arguments = new Object[argsAsArray.length + 1];
					arguments[0] = record;
					for (int i = 1; i != arguments.length; i++) {
						arguments[i] = argsAsArray[i - 1];
					}
					return method.invoke(target, arguments);
				} else {
					return method.invoke(target, record);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException ex) {
				throw new RuntimeException(String.format(
						"Exception thrown when calling method: [%s], arguments: [%s]", method, arg), ex);
			}
		}

	}

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired
	private List<MarcRecordFunctions> functionsList;

	private Map<String, MarcRecordFunction> functions = new HashMap<>();;

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
		for (final MarcRecordFunctions target : functionsList) {
			for (Method method : target.getClass().getMethods()) {
				if (isFunction(method)) {
					MarcRecordFunction func = new MarcRecordFunctionImpl(
							target, method);
					functions.put(method.getName(), func);
				}
			}
		}
	}

	public boolean isFunction(Method method) {
		Parameter[] parameters = method.getParameters();
		return Modifier.isPublic(method.getModifiers()) &&
				parameters.length > 0 && parameters[0].getType().isAssignableFrom(MarcRecord.class);
	}

}
