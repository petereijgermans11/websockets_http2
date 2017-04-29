package nl.campanula.jee7showcase.websocket;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import javax.websocket.DeploymentException;

import nl.campanula.jee7showcase.websocket.client.LoggingEchoingExternalEndpointWebSocketClient;
import nl.campanula.jee7showcase.websocket.server.LoggingEchoingConvertingWebSocketServerEndpoint;

import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.awaitility.Duration;

public class ExternalEndpointWebSocketClientTest {
	private static final String CONTEXT_ROOT = "/websocket";
	private static final int PORT = 8025;
	private static final String HOST = "localhost";

	private static Server server;

	private final LoggingEchoingExternalEndpointWebSocketClient client = new LoggingEchoingExternalEndpointWebSocketClient();

	/**
	 * Starts a server to talk to.
	 */
	@BeforeClass
	public static void beforeClass() {
		server = new Server(HOST, PORT, CONTEXT_ROOT, LoggingEchoingConvertingWebSocketServerEndpoint.class);
		try {
			server.start();
		} catch (final DeploymentException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void afterClass() {
		server.stop();
	}

	@Before
	public void setUp() throws URISyntaxException, DeploymentException, IOException {
		client.initialize(HOST, PORT, CONTEXT_ROOT, "myOtherWickedClientId");
	}

	@After
	public void tearDown() throws IOException {
		client.close();
	}

	private Callable<Boolean> connectionIsClosed() {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return !client.isOpen();
			}
		};
	}

	@Test
	public void testSend() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		assertTrue(client.isOpen());
		client.sendMessage("{\"int\":42}");
		assertEquals("Hi there", client.waitForResponse());
		assertEquals("43", client.waitForResponse());
		// JSON-encoded Person
		assertTrue(client.waitForResponse().contains("foo@bar"));
		client.sendMessage("{\"int\":666}");
		await().pollInterval(Duration.ONE_HUNDRED_MILLISECONDS) //
				.atMost(5, SECONDS) //
				.until(connectionIsClosed());
	}
}
