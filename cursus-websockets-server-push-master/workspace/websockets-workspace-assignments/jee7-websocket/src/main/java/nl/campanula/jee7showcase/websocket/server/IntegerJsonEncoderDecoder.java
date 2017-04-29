package nl.campanula.jee7showcase.websocket.server;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder and decoder for "{"int":42}"-like JSON
 */
public class IntegerJsonEncoderDecoder implements Encoder.Text<Integer>, Decoder.Text<Integer> {
	private static final Logger LOGGER = LoggerFactory.getLogger(IntegerJsonEncoderDecoder.class);

	private static final String JSON_INT_FIELD_NAME = "int";

	@Override
	public String encode(final Integer value) throws EncodeException {
		final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

		objectBuilder.add(JSON_INT_FIELD_NAME, String.valueOf(value));
		final JsonObject jsonObject = objectBuilder.build();
		final StringWriter sw = new StringWriter();
		try (JsonWriter writer = Json.createWriter(sw)) {
			writer.writeObject(jsonObject);
		}
		return sw.getBuffer().toString();
	}

	@Override
	public boolean willDecode(final String encoded) {
		try {
			decode(encoded);
		} catch (final Throwable t) {
			return false;
		}
		return true;
	}

	@Override
	public Integer decode(final String encoded) throws DecodeException {
		LOGGER.info("Decode '{}'", encoded);
		final StringReader reader = new StringReader(encoded);
		final JsonObject jsonObject = Json.createReader(reader).readObject();
		reader.close();

		return jsonObject.getInt(JSON_INT_FIELD_NAME);
	}

	@Override
	public void init(final EndpointConfig endpointConfig) {
		LOGGER.info("Initialization for {} ", endpointConfig);
	}

	@Override
	public void destroy() {
		// nothing
	}

}
