package nl.ordina.jtech.ws.jerseyjaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJaxrsApplication extends Application {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncJaxrsApplication.class);

	@Override
	public Set<Class<?>> getClasses() {
		LOG.info("starting async JAX-RS");
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(EventResource.class);
		return classes;
	}
}
