package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public abstract class AbstractScriptFactory<T> implements MappingScriptFactory<T> {

	private static Logger logger = LoggerFactory.getLogger(AbstractScriptFactory.class);
	
	@Override
	public MappingScript<T> create(InputStream... scripts) {
		final List<byte[]> copy = copy(scripts);
		return new MappingScript<T>() {

			ThreadLocal<MappingScript<T>> delegate = new ThreadLocal<MappingScript<T>>(){
				
			    protected MappingScript<T> initialValue() {
			        return innerCreate(copy);
			    }
				
			};
			
			@Override
			public Map<String, Object> parse(final T record) {
				return delegate.get().parse(record);
			}
			
		};
	}
	
	private MappingScript<T> innerCreate(List<byte[]> scriptsSource) {
		logger.info("About to create new mapping script");
		final AssignmentEliminationTransformation astTransformer = new AssignmentEliminationTransformation();
		final Binding binding = createBinding();
		final List<DelegatingScript> scripts = new ArrayList<DelegatingScript>(scriptsSource.size());
		final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.setScriptBaseClass(DelegatingScript.class
				.getCanonicalName());
		compilerConfiguration.addCompilationCustomizers(
				new SecureASTCustomizer(), new ASTTransformationCustomizer(astTransformer));
		for (byte[] scriptSource : scriptsSource) {
			GroovyShell sh = new GroovyShell(binding, compilerConfiguration);
			final DelegatingScript script = (DelegatingScript) sh
					.parse(new InputStreamReader(new ByteArrayInputStream(scriptSource)));
			scripts.add(script);
			astTransformer.getVariablesToExclude().addAll(astTransformer.getVariablesToExclude());
		}
		Collections.reverse(scripts);
		MappingScript<T> result = create(binding, scripts);
		logger.info("New mapping script created");
		return result;
	}
	
	protected abstract MappingScript<T> create(Binding binding, List<DelegatingScript> scripts);
	
	private Binding createBinding() {
		return new Binding();
	}
	
	private List<byte[]> copy(InputStream... scripts) {
		List<byte[]> copy = new ArrayList<byte[]>(scripts.length);
		for (InputStream res : scripts) {
			try (InputStream resource = res) {
				byte[] content = ByteStreams.toByteArray(resource);
				copy.add(content);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return copy;
	}

}
