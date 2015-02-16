package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

public abstract class AbstractScriptFactory<T> implements MappingScriptFactory<T> {

	@Override
	public MappingScript<T> create(InputStream... scriptsSource) {
		final AssignmentEliminationTransformation astTransformer = new AssignmentEliminationTransformation(); 
		final Binding binding = new Binding();
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
	
}
