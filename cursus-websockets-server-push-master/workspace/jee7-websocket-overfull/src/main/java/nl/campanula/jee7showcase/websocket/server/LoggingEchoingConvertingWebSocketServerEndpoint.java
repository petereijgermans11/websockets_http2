package nl.campanula.jee7showcase.websocket.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Container creates 1 instance *per connection*!
 * 
 * Can also extend javax.websocket.Endpoint base class.
 */
@ServerEndpoint(value = "/myWebSocketEndpoint/{myId}" // url *na* ctxroot
, configurator = LoggingDelegatingServerEndpointConfigurator.class //
, encoders = { IntegerJsonEncoderDecoder.class, PersonJsonEncoderDecoder.class } //
, decoders = { IntegerJsonEncoderDecoder.class, PersonJsonEncoderDecoder.class } //
/* , subprotocols={"chat"} */
)
public class LoggingEchoingConvertingWebSocketServerEndpoint {
	private static final int POISON_PILL = 666;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEchoingConvertingWebSocketServerEndpoint.class);

	@OnOpen
	public void onOpen(final Session client) {
		// when storing client data here, do that in a threadsafe set!
		LOGGER.info("Adding client with id {}, # open: {}", 
				client != null ? client.getId() : "?", 
				(client != null && client.getOpenSessions() != null) ? client.getOpenSessions().size() : -1);
	}

	@OnClose
	public void onClose(final Session client) {
		LOGGER.info("Removing client {}", client);
	}

	@OnError
	public void onError(final Session client, final Throwable throwable) throws Exception {
		LOGGER.error(client.toString(), throwable);
	}

	/*
	 * Handles String messages that can be decoded. Note!!! non-decodable
	 * messages seem to be dropped silently?!
	 */
	@OnMessage(maxMessageSize = 10000)
	// method param are connection init params; *NOT* datagrams!
	public void onJSONDecodableMessage(@PathParam("myId") final String someClientId, 
			// final String message, - use this when not using de/encoder
			final Integer autoDecodedMessagePayload, final Session client) throws IOException {
		LOGGER.info("Received message from {} for {}, decoded from JSON as: {}, will re-distribute to {} clients",
				new Object[] { client, someClientId, autoDecodedMessagePayload, (client != null && client.getOpenSessions() != null) ? client.getOpenSessions().size() : -1 });

		for (final Session peer : client.getOpenSessions()) {
			if (peer.isOpen() /*
							 * && !client.equals(peer) - enable this to NOT echo
							 * to sender
							 */) {
				if (autoDecodedMessagePayload == POISON_PILL) {
					LOGGER.info("Got poison pill from {}", peer);
					try {
						peer.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "took a poison pill"));
					} catch (final IOException e) {
						LOGGER.info("Got exception when closing - ignoring", e);
					}
					continue;
				}
				LOGGER.info("Sending message to {}", peer);
				try {
					// synchronous sending
					peer.getBasicRemote().sendText("Hi there");
					peer.getBasicRemote().sendObject(1 + autoDecodedMessagePayload);
					peer.getBasicRemote().sendObject(new Person(36, "Hedzer", "Westra", "foo@bar"));
					// async sending - not tried yet
					// peer.getAsyncRemote().sendBinary(new ByteBuffer());
				} catch (IOException | NullPointerException | EncodeException e) {
					LOGGER.error("Got exception when sending, closing peer", e);
					try {
						peer.close();
					} catch (final Exception e1) {
						LOGGER.warn("ignoring error {} ", e1.toString());
					}
				}
			}
		}
	}

	/*
	 * Handles binary messages. Note!! Invocation order is nondeterministic when
	 * text, binary, etc. messages are mixed.
	 */
	@OnMessage
	public void onBinaryMessage(@PathParam("myId") final String someClientId, final ByteBuffer message,
			final Session client) throws IOException {
		LOGGER.info("Received binary message from {} for {}, '{}', will re-distribute to {} clients", new Object[] {
				client, someClientId, new String(message.array()), client.getOpenSessions().size() });

		for (final Session peer : client.getOpenSessions()) {
			if (peer.isOpen()) {
				LOGGER.info("Sending back binary and text message to {}", peer);
				try {
					final ByteBuffer data = ByteBuffer.allocate(10);
					data.put((byte) 0x30);
					data.put((byte) 0x41);
					peer.getBasicRemote().sendBinary(data);

					peer.getBasicRemote().sendText(String.format("Signing off '%s'", new String(message.array())));
				} catch (IOException | NullPointerException e) {
					LOGGER.error("Got exception when sending, closing peer", e);
					try {
						peer.close();
					} catch (final Exception e1) {
						LOGGER.warn("ignoring error {} ", e1.toString());
					}
				}
			}
		}
	}
}
