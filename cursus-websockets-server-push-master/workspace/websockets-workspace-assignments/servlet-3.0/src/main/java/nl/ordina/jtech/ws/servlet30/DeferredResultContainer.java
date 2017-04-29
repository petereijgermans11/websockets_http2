package nl.ordina.jtech.ws.servlet30;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/** 
 * Manages and updates a set of Spring DeferredResult's.
 */
@Service
public class DeferredResultContainer {
	private static final Logger LOG = LoggerFactory.getLogger(DeferredResultContainer.class);

	private final Set<DeferredResult<String>> deferredResults = Collections
			.synchronizedSet(new HashSet<DeferredResult<String>>());

	public void put(final DeferredResult<String> deferredResult) {
		deferredResults.add(deferredResult);
	}

	public void updateAllResults(final String value) {
		for (final DeferredResult<String> deferredResult : deferredResults) {
			LOG.info("triggering onCompletion");

			// TODO set 'value' as result of the deferred result
			...
		}
	}

	public void remove(final DeferredResult<String> deferredResult) {
		deferredResults.remove(deferredResult);
	}
}