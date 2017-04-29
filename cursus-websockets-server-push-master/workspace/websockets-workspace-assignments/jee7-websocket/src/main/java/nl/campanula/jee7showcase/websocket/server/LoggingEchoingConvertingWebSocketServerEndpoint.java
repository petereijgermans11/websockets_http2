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

@ServerEndpoint(value = "/myWebSocketEndpoint/{myId}"
, encoders = { IntegerJsonEncoderDecoder.class, PersonJsonEncoderDecoder.class } //
, decoders = { IntegerJsonEncoderDecoder.class, PersonJsonEncoderDecoder.class } //
)
public class LoggingEchoingConvertingWebSocketServerEndpoint {
	private static final int POISON_PILL = 666;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEchoingConvertingWebSocketServerEndpoint.class);

	// TODO annotate this method properly
	public void onOpen(final Session client) {
		// note: when storing client data here, do that in a threadsafe set!
		LOGGER.info("New client with id {}, # open: {}", 
				client != null ? client.getId() : "?", 
				(client != null && client.getOpenSessions() != null) ? client.getOpenSessions().size() : -1);
	}

	// TODO annotate this method properly
	public void onClose(final Session client) {
		LOGGER.info("Close client {}", client);
	}

	// TODO annotate this method properly
	public void onError(final Session client, final Throwable throwable) throws Exception {
		LOGGER.error(client.toString(), throwable);
	}

	/*
	 * Handles String messages that can be decoded. 
	 * Note:
	 * - non-decodable messages seem to be dropped silently?!
	 * - method params ('myId') are connection init params; *NOT* datagrams!
	 */
	// TODO annotate this method to accept messages up to 10000 B in size
	public void onJSONDecodableMessage(@PathParam("myId") final String someClientId, 
			final Integer autoDecodedMessagePayload, final Session client) throws IOException {
		LOGGER.info("Received message from {} for {}, decoded from JSON as: {}, will re-distribute to {} clients",
				new Object[] { client, someClientId, autoDecodedMessagePayload, (client != null && client.getOpenSessions() != null) ? client.getOpenSessions().size() : -1 });

		// TODO loop over all the client's open sessions

			if (autoDecodedMessagePayload == POISON_PILL) {
				LOGGER.info("Got poison pill from {}", peer);
				try {
					// TODO send an appropriate close code
					peer.close(new CloseReason(..., "took a poison pill"));
				} catch (final IOException e) {
					LOGGER.info("Got exception when closing - ignoring", e);
				}
				continue;
			}
			LOGGER.info("Sending message to {}", peer);
			try {
				// TODO get peer's "basic remote" (meaning synchronous) and then:
				// - send text "Hi there"
				// - send object: 1 + autoDecodedMessagePayload
				// - send a Person with some age, first name, last name and email
			} catch (IOException | NullPointerException | EncodeException e) {
				LOGGER.error("Got exception when sending, closing peer", e);
				try {
					peer.close();
				} catch (final Exception e1) {
					LOGGER.warn("ignoring error {} ", e1.toString());
				}
			}
		// end loop
	}

	/*
	 * Handles binary messages. Note: invocation order is nondeterministic when
	 * text, binary, etc. messages are mixed.
	 * 
	 * This method is complete - no need to add or change anything.

	 * It's here for illustration purposes - i.e., how to handle binary data
	 */
	@OnMessage
	public void onBinaryMessage(@PathParam("myId") final String someClientId, final ByteBuffer message,
			final Session client) throws IOException {
		LOGGER.info("Received binary message from {} for {}, '{}', will re-distribute to {} clients", new Object[] {
				client, someClientId, new String(message.array()), client.getOpenSessions().size() });

		for (final Session peer : client.getOpenSessions()) {
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
