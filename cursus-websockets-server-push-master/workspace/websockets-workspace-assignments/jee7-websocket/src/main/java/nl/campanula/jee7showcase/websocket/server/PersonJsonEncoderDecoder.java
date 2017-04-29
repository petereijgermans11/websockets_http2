package nl.campanula.jee7showcase.websocket.server;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes *and* decodes a Person DTO to/from a JSON Text object for use by JEE Websocket endpoints.
 * (and that's a hint!)
 * 
 * A Jackson ObjectMapper is used for easy POJO transformation.
 */
public class PersonJsonEncoderDecoder implements ... /*TODO*/ {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonJsonEncoderDecoder.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final JsonFactory JSON_FACTORY = new JsonFactory();

	@Override
	public String encode(final Person person) throws EncodeException {
		final StringWriter sw = new StringWriter();
		JsonGenerator generator;
		try {
			generator = JSON_FACTORY.createJsonGenerator(sw);
			OBJECT_MAPPER.writeValue(generator, person);
		} catch (final IOException e) {
			LOGGER.error("could not encode: {}", e);
			throw new EncodeException(person, "", e);
		}
		return sw.toString();
	}

	@Override
	public boolean willDecode(final String encoded) {
		try {
			OBJECT_MAPPER.readValue(encoded, Person.class);
		} catch (final Throwable t) {
			return false;
		}
		return true;
	}

	@Override
	public Person decode(final String encoded) throws DecodeException {
		try {
			return OBJECT_MAPPER.readValue(encoded, Person.class);
		} catch (final IOException e) {
			LOGGER.error("could not decode: {}", e);
			throw new DecodeException(encoded, "", e);
		}
	}

	@Override
	public void init(final EndpointConfig config) {
		// nothing required
	}

	@Override
	public void destroy() {
		// nothing required
	}

}
