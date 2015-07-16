package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

public abstract class AbstractScriptFactory<T> implements MappingScriptFactory<T> {

	@Override
	public MappingScript<T> create(InputStream... scriptsSource) {
		final AssignmentEliminationTransformation astTransformer = new AssignmentEliminationTransformation();
		final Binding binding = createBinding();
		final List<DelegatingScript> scripts = new ArrayList<DelegatingScript>(scriptsSource.length);
		final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.setScriptBaseClass(DelegatingScript.class
				.getCanonicalName());
		compilerConfiguration.addCompilationCustomizers(
				new SecureASTCustomizer(), new ASTTransformationCustomizer(astTransformer));
		for (InputStream scriptSource : scriptsSource) {
			GroovyShell sh = new GroovyShell(binding, compilerConfiguration);
			final DelegatingScript script = (DelegatingScript) sh
					.parse(new InputStreamReader(scriptSource));
			scripts.add(script);
			astTransformer.getVariablesToExclude().addAll(astTransformer.getVariablesToExclude());
		}
		Collections.reverse(scripts);
		return create(binding, scripts);
	}
	
	protected abstract MappingScript<T> create(Binding binding, List<DelegatingScript> scripts);
	
	private Binding createBinding() {
		Map<Object, Object> map = new Map<Object, Object>() {

			private ThreadLocal<Map<Object, Object>> delegate = new ThreadLocal<Map<Object, Object>>() {

				@Override
				protected Map<Object, Object> initialValue() {
					return new HashMap<Object, Object>();
				}
				
			};
			
			@Override
			public int size() {
				return delegate.get().size();
			}

			@Override
			public boolean isEmpty() {
				return delegate.get().isEmpty();
			}

			@Override
			public boolean containsKey(Object key) {
				return delegate.get().containsKey(key);
			}

			@Override
			public boolean containsValue(Object value) {
				return delegate.get().containsValue(value);
			}

			@Override
			public Object get(Object key) {
				return delegate.get().get(key);
			}

			@Override
			public Object put(Object key, Object value) {
				return delegate.get().put(key, value);
			}

			@Override
			public Object remove(Object key) {
				return delegate.get().remove(key);
			}

			@Override
			public void putAll(Map<? extends Object, ? extends Object> m) {
				delegate.get().putAll(m);
			}

			@Override
			public void clear() {
				delegate.get().clear();
			}

			@Override
			public Set<Object> keySet() {
				return delegate.get().keySet();
			}

			@Override
			public Collection<Object> values() {
				return delegate.get().values();
			}

			@Override
			public Set<java.util.Map.Entry<Object, Object>> entrySet() {
				return delegate.get().entrySet();
			}
			
		};
		final Binding binding = new Binding(map);
		return binding;
	}
	
}
