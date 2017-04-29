package nl.campanula.jee7showcase.websocket.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A client websocket endpoint can initiate connections, but other than that is very much alike a
// server endpoint.
@ClientEndpoint
public class LoggingWebSocketClientEndpoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingWebSocketClientEndpoint.class);

	private final BlockingQueue<String> queue;

	public LoggingWebSocketClientEndpoint() {
		queue = new ArrayBlockingQueue<>(10);
	}

	@OnOpen
	public void onOpen(final Session client) {
		LOGGER.info("OPEN {}", client);
	}

	@OnClose
	public void onClose(final Session client) {
		LOGGER.info("CLOSE {}", client);
	}

	@OnError
	public void onError(final Session client, final Throwable throwable) throws Exception {
		LOGGER.info("ERROR {}: {}", client.toString(), throwable);
	}

	@OnMessage
	public void message(final String message, final Session client) throws InterruptedException {
		LOGGER.info("Client endpoint received message '{}' from server {}", message, client);
		queue.put(message);
	}

	public String getMessageFromQueue() throws InterruptedException {
		return queue.take();
	}

}
