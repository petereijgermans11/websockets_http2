package nl.ordina.jtech.log;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.stereotype.Component;

@Component
public class JULInitializer {

	public JULInitializer() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JULInitializer.class.getName());
		logger.info("Logging from JUL");
	}
}
