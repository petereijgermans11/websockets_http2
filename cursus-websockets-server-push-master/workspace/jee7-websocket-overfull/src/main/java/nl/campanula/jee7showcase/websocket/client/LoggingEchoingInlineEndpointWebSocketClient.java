package nl.campanula.jee7showcase.websocket.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEchoingInlineEndpointWebSocketClient implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEchoingInlineEndpointWebSocketClient.class);

	private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

	private Session session;

	private Endpoint endpoint;

	public void initialize(final String host, final int port, final String context, final String clientId)
			throws URISyntaxException, DeploymentException, IOException {
		// embedded Server used for unit testing seems to ignore or downgrade
		// 'wss' - no SSL/HTTPS used...
		final URI uri = new URI(String.format("wss://%s:%d%s/myWebSocketEndpoint/%s", host, port, context, clientId));
		final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

		endpoint = new Endpoint() {
			@Override
			public void onOpen(final Session session, final EndpointConfig endpointConfig) {
				LOGGER.info("CONNECTED");

				try {
					// server will drop this msg, since it is not
					// JSON-to-int-decodable
					session.getBasicRemote().sendText("Initial text from client");

					final ByteBuffer data = ByteBuffer.allocate(10);
					data.put((byte) 0x31);
					data.put((byte) 0x42);
					session.getBasicRemote().sendBinary(data);

					LOGGER.info("Did 2 remote calls from onOpen");
				} catch (final IOException e) {
					LOGGER.error("exc: {}", e);
				}

				session.addMessageHandler(new MessageHandler.Whole<String>() {
					@Override
					public void onMessage(final String message) {
						try {
							queue.put(message);
							LOGGER.info("RECEIVED text message: {}", message);
							if (session.isOpen()) {
								LOGGER.info("sending back an echo");
								session.getBasicRemote().sendText("Echo: " + message);
							}
						} catch (final IOException | InterruptedException ex) {
							LOGGER.error("oops: {}", ex);
						}
					}
				});
			}

			@Override
			public void onClose(final Session session, final CloseReason closeReason) {
				LOGGER.info("CloseReason: " + closeReason);
			}

			@Override
			public void onError(final Session session, final Throwable thr) {
				LOGGER.error("ON ERROR {}", thr);
			}
		};

		session = container.connectToServer(endpoint, ClientEndpointConfig.Builder.create().build(), uri);

		LOGGER.info("Is open  : " + session.isOpen());
		LOGGER.info("Is secure: " + session.isSecure());

		// can only add one msghandler *per type* - text handler is added inline
		// above.
		session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
			@Override
			public void onMessage(final ByteBuffer message) {
				LOGGER.info("RECEIVED binary message: {}", new String(message.array()));
			}
		});
	}

	public void sendMessage(final String message) throws IOException {
		session.getBasicRemote().sendText(message);
	}

	public String waitForResponse() throws InterruptedException {
		return queue.take();
	}

	@Override
	public void close() throws IOException {
		if (session != null) {
			session.close();
		}
	}

}