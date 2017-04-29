package nl.campanula.jee7showcase.websocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;

import nl.campanula.jee7showcase.websocket.client.LoggingEchoingInlineEndpointWebSocketClient;
import nl.campanula.jee7showcase.websocket.server.LoggingEchoingConvertingWebSocketServerEndpoint;

import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class InlineEndpointWebSocketClientTest {
	private static final String CONTEXT_ROOT = "/websocket";
	private static final int PORT = 8025;
	private static final String HOST = "localhost";

	private static Server server;

	private final LoggingEchoingInlineEndpointWebSocketClient client = new LoggingEchoingInlineEndpointWebSocketClient();

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
		client.initialize(HOST, PORT, CONTEXT_ROOT, "myWickedClientId");
	}

	@After
	public void tearDown() throws IOException {
		client.close();
	}

	@Test
	public void testSend() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		assertTrue(client.waitForResponse().startsWith("Signing off '1B"));
		client.sendMessage("{\"int\":42}");
		assertEquals("Hi there", client.waitForResponse());
		assertEquals("43", client.waitForResponse());
		assertTrue(client.waitForResponse().contains("foo@bar"));
	}
}
