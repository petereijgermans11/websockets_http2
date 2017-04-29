package nl.ordina.jtech.ws.servlet30;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Schedules updating all then-known DeferredResult's. Runs every 5s. 
 * Fakes a slow backend by waiting 2s and then setting the value "GAZILLION", always.
 * 
 * Scheduled code runs using the context-configured asyncTaskExecutor.
 * 
 * This code is complete.
 */
@Service
public class QuoteUpdateScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(QuoteUpdateScheduler.class);

	@Autowired
	DeferredResultContainer deferredResultContainer;

	@Scheduled(fixedRate = 5000)
	public void process() {
		LOG.debug("quote scheduled");
		deferredResultContainer.updateAllResults(getQuoteValueFromSlowResource());
	}

	private String getQuoteValueFromSlowResource() {
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
		}
		return "GAZILLION";
	}
}
