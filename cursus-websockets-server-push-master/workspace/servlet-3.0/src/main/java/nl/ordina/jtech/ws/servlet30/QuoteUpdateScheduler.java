package nl.ordina.jtech.ws.servlet30;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class QuoteUpdateScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(QuoteUpdateScheduler.class);

	@Autowired
	DeferredResultContainer deferredResultContainer;

	@Scheduled(fixedRate = 5000)
	public void process() {
		LOG.info("quote scheduled");
		deferredResultContainer.updateAllResults(getQuoteValueFromSlowResource());
	}

	private String getQuoteValueFromSlowResource() {
		LOG.info("faking quote retrieval by 2s sleep");
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			// ignore
		}
		LOG.info("2s sleep done");
		return "GAZILLION";
	}
}
