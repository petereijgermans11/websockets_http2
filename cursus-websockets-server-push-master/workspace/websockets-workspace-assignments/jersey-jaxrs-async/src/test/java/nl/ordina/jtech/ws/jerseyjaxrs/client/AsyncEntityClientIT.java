package nl.ordina.jtech.ws.jerseyjaxrs.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ServiceUnavailableException;

import nl.ordina.jtech.ws.jerseyjaxrs.AsyncEntityClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integratietest - start een embedded Jetty en vuurt daarop via een JAX-RS client enkele requests af.
 * De client zelf is (momenteel) synchroon.
 */
public class AsyncEntityClientIT {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncEntityClientIT.class);

	private static final int PORT = 8888;
	private static final String CTX_PATH = "/jersey-jaxrs-async";
	private static final String SVC_PATH = "/async-webapi";
	private static final String EMBEDDED_JETTY_URL = String
			.format("http://localhost:%d%s%s/", PORT, CTX_PATH, SVC_PATH);

	private final AsyncEntityClient client = new AsyncEntityClient(EMBEDDED_JETTY_URL);

	private static Server server;

	@BeforeClass
	public static void startEmbeddedJetty() throws Exception {
		LOG.info("starting embedded Jetty");
		server = new Server(PORT);
		server.setStopAtShutdown(true);

		final WebAppContext wac = new WebAppContext();
		wac.setContextPath(CTX_PATH);
		wac.setResourceBase("src/main/webapp");
		wac.setClassLoader(AsyncEntityClientIT.class.getClassLoader());
		server.setHandler(wac);

		server.start();
	}

	@Test
	public void shouldGetSomeResponseFromEmbeddedJetty() throws Exception {
		final String userId = "invalid";
		final HttpClient client = HttpClientBuilder.create().build();
		final HttpGet mockRequest = new HttpGet(String.format("http://localhost:%d%s/", PORT, "some-fake-ctx-root"));
		mockRequest.setHeader("http-user", userId);
		final HttpResponse mockResponse = client.execute(mockRequest);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(mockResponse.getEntity().getContent()));
		assertEquals("<html>", rd.readLine());
	}

	@AfterClass
	public static void shutdownEmbeddedJetty() throws Exception {
		// atmosphere always crashes with NPE when closing. Aargh!
		// o.e.jetty.servlet.ServletHolder catches it and logs at WARN..
		server.stop();
	}

	@Test(expected = ServiceUnavailableException.class)
	public void shouldGetTimeoutOnEntityList() {
		client.getEntityList();
	}

	@Test
	public void shouldGetEntityAfterSomeTime() throws InterruptedException, ExecutionException, TimeoutException {
		final String entity = client.getEntity(42);
		assertEquals("You sent me entityId 42", entity);
	}

	@Test
	public void shouldCreateEntityAfterSomeTime() {
		final String entity = client.createEntity("fubar");
		assertEquals("Created entity with name fubar", entity);
	}

}
