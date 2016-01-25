package cz.mzk.recordmanager.server.automatization;

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class ScriptRunnerImpl implements ScriptRunner {

	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	private ResourceProvider provider;

	private final String root = "/automatization/";

	@Override
	public void run(String scriptPath) {
		InputStream script = null;
		try {
			script = provider.getResource(root + scriptPath);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		try (GroovyClassLoader loader = new GroovyClassLoader()) {
			Class<?> groovyClass = loader.parseClass(ResourceUtils.asString(script));
			Runnable runnable = (Runnable) groovyClass.newInstance();
			init(runnable);
			runnable.run();
		} catch (CompilationFailedException | IOException | IllegalAccessException | InstantiationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void init(Object script) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(script);
		factory.initializeBean(script, "script");
	}

}
