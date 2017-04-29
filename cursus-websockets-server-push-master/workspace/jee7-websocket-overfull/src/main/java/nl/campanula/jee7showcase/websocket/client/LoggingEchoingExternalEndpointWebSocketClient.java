package nl.campanula.jee7showcase.websocket.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEchoingExternalEndpointWebSocketClient implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEchoingExternalEndpointWebSocketClient.class);

	private Session session;
	private LoggingWebSocketClientEndpoint endpoint;

	public void initialize(final String host, final int port, final String context, final String clientId)
			throws URISyntaxException, DeploymentException, IOException {
		final URI uri = new URI(String.format("ws://%s:%d%s/myWebSocketEndpoint/%s", host, port, context, clientId));
		final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

		// use this for CDI:
		// session =
		// container.connectToServer(LoggingWebSocketClientEndpoint.class, uri);

		// use this for direct (as used here) or Spring DI:
		endpoint = new LoggingWebSocketClientEndpoint();
		session = container.connectToServer(endpoint, uri);

		LOGGER.debug("Is secure: " + session.isSecure());
	}

	public boolean isOpen() {
		LOGGER.info("Is open: " + session.isOpen());
		return session.isOpen();
	}

	public void sendMessage(final String message) throws IOException {
		session.getBasicRemote().sendText(message);
	}

	public String waitForResponse() throws InterruptedException {
		return endpoint.getMessageFromQueue();
	}

	@Override
	public void close() throws IOException {
		if (session != null) {
			session.close();
		}
	}

}