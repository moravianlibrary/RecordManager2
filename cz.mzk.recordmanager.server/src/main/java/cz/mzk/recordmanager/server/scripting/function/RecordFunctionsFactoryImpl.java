package cz.mzk.recordmanager.server.scripting.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class RecordFunctionsFactoryImpl implements RecordFunctionsFactory {

	private static class RecordFunctionImpl<T> implements RecordFunction<T> {

		private final Object target;

		private final Method method;

		public RecordFunctionImpl(Object target, Method method) {
			super();
			this.target = target;
			this.method = method;
		}

		@Override
		public Object apply(T record, Object args) {
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

	@Override
	public <T, S extends Object> Map<String, RecordFunction<T>> create(Class<T> clazz, Collection<S> targets) {
		if (targets == null) {
			targets = Collections.emptyList();
		}
		Map<String, RecordFunction<T>> functions = new HashMap<>();
		for (Object target : targets) {
			for (Method method : target.getClass().getMethods()) {
				if (isFunction(clazz, method)) {
					RecordFunctionImpl<T> func = new RecordFunctionImpl<T>(
							target, method);
					functions.put(method.getName(), func);
				}
			}
		}
		return functions;
	}

	protected <T> boolean isFunction(Class<T> clazz, Method method) {
		Parameter[] parameters = method.getParameters();
		return Modifier.isPublic(method.getModifiers()) &&
				parameters.length > 0 && parameters[0].getType().isAssignableFrom(clazz);
	}

}
