package nl.campanula.jee7showcase.websocket.server;

import java.util.List;

import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingDelegatingServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDelegatingServerEndpointConfigurator.class);

	@Override
	public String getNegotiatedSubprotocol(final List<String> supported, final List<String> requested) {
		LOGGER.info("getNegotiatedSubprotocol: supported={}, requested={}", supported, requested);
		return super.getNegotiatedSubprotocol(supported, requested);
	}

	@Override
	public List<Extension> getNegotiatedExtensions(final List<Extension> installed, final List<Extension> requested) {
		LOGGER.info("getNegotiatedExtensions: installed={}, requested={}", installed, requested);
		return super.getNegotiatedExtensions(installed, requested);
	}

	@Override
	public boolean checkOrigin(final String originHeaderValue) {
		LOGGER.info("checkOrigin: originHeaderValue={}", originHeaderValue);
		return super.checkOrigin(originHeaderValue);
	}

	@Override
	public void modifyHandshake(final ServerEndpointConfig seConfig, final HandshakeRequest request,
			final HandshakeResponse response) {
		LOGGER.info("modifyHandshake: req={}", request);
		super.modifyHandshake(seConfig, request, response);
	}

	@Override
	public <T> T getEndpointInstance(final Class<T> endpointClass) throws InstantiationException {
		LOGGER.info("getEndpointInstance for {}", endpointClass);
		return super.getEndpointInstance(endpointClass);
	}

}
