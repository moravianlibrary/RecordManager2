package cz.mzk.recordmanager.server.scripting.dc;

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

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.AbstractScriptFactory;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.dc.function.DublinCoreRecordFunction;
import cz.mzk.recordmanager.server.scripting.dc.function.DublinCoreRecordFunctions;

@Component
public class DublinCoreScriptFactoryImpl extends AbstractScriptFactory<DublinCoreRecord>
		implements DublinCoreScriptFactory, InitializingBean {

	private static class DublinCoreRecordFunctionImpl implements DublinCoreRecordFunction {

		private final DublinCoreRecordFunctions target;

		private final Method method;

		public DublinCoreRecordFunctionImpl(DublinCoreRecordFunctions target, Method method) {
			super();
			this.target = target;
			this.method = method;
		}

		@Override
		public Object apply(DublinCoreRecord record, Object args) {
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

	@Autowired(required=false)
	private List<DublinCoreRecordFunctions> functionsList;

	private Map<String, DublinCoreRecordFunction> functions = new HashMap<>();;

	@Override
	public DublinCoreMappingScript create(InputStream... scriptsSource) {
		return (DublinCoreMappingScript) super.create(scriptsSource);
	}

	@Override
	protected DublinCoreMappingScript create(Binding binding,
			List<DelegatingScript> scripts) {
		return new DublinCoreMappingScriptImpl(binding, scripts, propertyResolver,
				functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (functionsList == null) {
			return;
		}
		for (final DublinCoreRecordFunctions target : functionsList) {
			for (Method method : target.getClass().getMethods()) {
				if (isFunction(method)) {
					DublinCoreRecordFunction func = new DublinCoreRecordFunctionImpl(
							target, method);
					functions.put(method.getName(), func);
				}
			}
		}
	}

	public boolean isFunction(Method method) {
		Parameter[] parameters = method.getParameters();
		return Modifier.isPublic(method.getModifiers()) &&
				parameters.length > 0 && parameters[0].getType().isAssignableFrom(DublinCoreRecord.class);
	}

}
